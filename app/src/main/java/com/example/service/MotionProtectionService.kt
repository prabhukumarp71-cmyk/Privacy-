package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.admin.TouchGuardDeviceAdminReceiver
import com.example.data.model.ProtectionSettings
import com.example.data.model.SensitivityLevel
import com.example.data.repository.ProtectionRepository
import com.example.ui.screens.PrivacyBlackoutActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

enum class ServiceProtectionState {
    DISARMED,
    ARMING,
    ARMED,
    TRIGGERED
}

data class LiveSensorData(
    val deltaAcc: Float = 0f,
    val accX: Float = 0f,
    val accY: Float = 0f,
    val accZ: Float = 0f,
    val gyroSpeed: Float = 0f,
    val proximityDistance: Float = -1f,
    val isNear: Boolean = false
)

class MotionProtectionService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var armingJob: Job? = null

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var proximitySensor: Sensor? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var repository: ProtectionRepository

    private var lastAccX = 0f
    private var lastAccY = 0f
    private var lastAccZ = 0f
    private var hasBaseline = false

    private var lastTriggerTimestamp = 0L
    private val cooldownMs = 3000L // prevent multi-firing within 3s

    private var toneGenerator: ToneGenerator? = null

    override fun onCreate() {
        super.onCreate()
        repository = ProtectionRepository.getInstance(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TouchGuard:MotionWakeLock")

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 80)
        } catch (_: Exception) { }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_START_PROTECTION -> {
                startForeground(NOTIFICATION_ID, buildNotification("Arming TouchGuard..."))
                startArmingSequence()
            }
            ACTION_DISARM -> {
                disarm()
            }
            ACTION_STOP_SERVICE -> {
                disarm()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification("TouchGuard Active"))
                startArmingSequence()
            }
        }

        return START_STICKY
    }

    private fun startArmingSequence() {
        val settings = repository.settings.value
        val delaySec = settings.armingDelaySeconds

        armingJob?.cancel()
        _protectionState.value = ServiceProtectionState.ARMING
        hasBaseline = false

        acquireWakeLock()
        registerSensors()

        if (delaySec <= 0) {
            finalizeArming()
            return
        }

        armingJob = serviceScope.launch {
            for (remaining in delaySec downTo 1) {
                _armingCountdown.value = remaining
                updateNotification("Arming in $remaining seconds... Place phone down")
                delay(1000)
            }
            finalizeArming()
        }
    }

    private fun finalizeArming() {
        _protectionState.value = ServiceProtectionState.ARMED
        _armingCountdown.value = 0
        repository.updateSettings { it.copy(isArmed = true) }
        updateNotification("ARMED • Touch & Pick-up Shield Active")
    }

    private fun disarm() {
        armingJob?.cancel()
        _protectionState.value = ServiceProtectionState.DISARMED
        _armingCountdown.value = 0
        repository.updateSettings { it.copy(isArmed = false) }
        unregisterSensors()
        releaseWakeLock()
        updateNotification("TouchGuard is Disarmed")
    }

    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
        hasBaseline = false
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(60 * 60 * 1000L) // 1 hour max
            }
        } catch (_: Exception) { }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) { }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                if (!hasBaseline) {
                    lastAccX = x
                    lastAccY = y
                    lastAccZ = z
                    hasBaseline = true
                    return
                }

                val deltaX = x - lastAccX
                val deltaY = y - lastAccY
                val deltaZ = z - lastAccZ
                val deltaAcc = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

                // Update smooth filter for baseline
                lastAccX = lastAccX * 0.85f + x * 0.15f
                lastAccY = lastAccY * 0.85f + y * 0.15f
                lastAccZ = lastAccZ * 0.85f + z * 0.15f

                _liveSensorData.value = _liveSensorData.value.copy(
                    deltaAcc = deltaAcc,
                    accX = x,
                    accY = y,
                    accZ = z
                )

                checkThresholdAndTrigger(
                    deltaAcc = deltaAcc,
                    gyroSpeed = _liveSensorData.value.gyroSpeed,
                    triggerSource = "Motion / Touch"
                )
            }

            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]
                val gyroSpeed = sqrt(gx * gx + gy * gy + gz * gz)

                _liveSensorData.value = _liveSensorData.value.copy(
                    gyroSpeed = gyroSpeed
                )

                if (gyroSpeed > 1.8f) {
                    checkThresholdAndTrigger(
                        deltaAcc = _liveSensorData.value.deltaAcc,
                        gyroSpeed = gyroSpeed,
                        triggerSource = "Phone Picked Up (Tilt / Rotation)"
                    )
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                val isNear = distance < maxRange

                val wasNear = _liveSensorData.value.isNear
                _liveSensorData.value = _liveSensorData.value.copy(
                    proximityDistance = distance,
                    isNear = isNear
                )

                val settings = repository.settings.value
                if (settings.pocketProximityMode && wasNear && !isNear && _protectionState.value == ServiceProtectionState.ARMED) {
                    checkThresholdAndTrigger(
                        deltaAcc = 2.0f,
                        gyroSpeed = 1.0f,
                        triggerSource = "Phone Taken Out of Pocket / Uncovered"
                    )
                }
            }
        }
    }

    private fun checkThresholdAndTrigger(deltaAcc: Float, gyroSpeed: Float, triggerSource: String) {
        if (_protectionState.value != ServiceProtectionState.ARMED) return

        val now = System.currentTimeMillis()
        if (now - lastTriggerTimestamp < cooldownMs) return

        val settings = repository.settings.value
        val threshold = settings.sensitivity.threshold

        val isDeltaTriggered = deltaAcc >= threshold
        val isGyroTriggered = gyroSpeed >= (threshold * 0.8f)

        if (isDeltaTriggered || isGyroTriggered) {
            lastTriggerTimestamp = now
            executeTriggerActions(settings, triggerSource, deltaAcc, gyroSpeed)
        }
    }

    private fun executeTriggerActions(
        settings: ProtectionSettings,
        reason: String,
        deltaAcc: Float,
        gyroSpeed: Float
    ) {
        _protectionState.value = ServiceProtectionState.TRIGGERED
        val readingDesc = String.format(Locale.US, "ΔAcc: %.2f m/s², Rot: %.2f rad/s", deltaAcc, gyroSpeed)
        val actionsList = mutableListOf<String>()

        // 1. Close apps and return to home
        if (settings.actionCloseAppReturnHome) {
            val closedViaA11y = TouchGuardAccessibilityService.instance?.closeActiveAppAndReturnHome() ?: false
            if (!closedViaA11y) {
                PrivacyBlackoutActivity.returnToHomeScreen(applicationContext)
            }
            actionsList.add("Closed Running App & Returned to Home")
        }

        // 2. Turn screen off and lock
        if (settings.actionLockScreenOff) {
            val lockedViaA11y = TouchGuardAccessibilityService.instance?.turnScreenOffAndLock() ?: false
            val lockedViaAdmin = if (!lockedViaA11y) {
                TouchGuardDeviceAdminReceiver.lockDevice(applicationContext)
            } else true

            if (lockedViaA11y || lockedViaAdmin) {
                actionsList.add("Turned Screen Off & Locked")
            } else {
                // If neither permission is active, blackout screen activity ensures apps are hidden
                val blackoutIntent = Intent(applicationContext, PrivacyBlackoutActivity::class.java).apply {
                    putExtra("EXTRA_REASON", reason)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(blackoutIntent)
                actionsList.add("Privacy Blackout Screen Deployed")
            }
        } else if (settings.actionPrivacyBlackout) {
            val blackoutIntent = Intent(applicationContext, PrivacyBlackoutActivity::class.java).apply {
                putExtra("EXTRA_REASON", reason)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(blackoutIntent)
            actionsList.add("Privacy Blackout Screen Deployed")
        }

        // 3. Vibration feedback
        if (settings.actionVibration) {
            triggerVibration()
        }

        // 4. Alarm sound
        if (settings.actionAlarmSound) {
            triggerSoundAlarm()
        }

        val actionSummary = if (actionsList.isNotEmpty()) actionsList.joinToString(" • ") else "Alert Triggered"

        // Record incident in Room DB
        serviceScope.launch {
            repository.recordIncident(
                reason = reason,
                sensorReading = readingDesc,
                actionTaken = actionSummary,
                alarmTriggered = settings.actionAlarmSound
            )
        }

        updateNotification("⚠️ ALERT: $reason at ${getCurrentTimeString()}")

        // Keep armed after trigger or re-arm after brief cooldown
        serviceScope.launch {
            delay(2000)
            if (_protectionState.value == ServiceProtectionState.TRIGGERED) {
                _protectionState.value = ServiceProtectionState.ARMED
            }
        }
    }

    private fun triggerVibration() {
        try {
            val pattern = longArrayOf(0, 300, 150, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
        } catch (_: Exception) { }
    }

    private fun triggerSoundAlarm() {
        try {
            serviceScope.launch(Dispatchers.IO) {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1500)
            }
        } catch (_: Exception) { }
    }

    private fun getCurrentTimeString(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TouchGuard Motion Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors phone touch and pick up in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disarmIntent = Intent(this, MotionProtectionService::class.java).apply {
            action = ACTION_DISARM
        }
        val disarmPendingIntent = PendingIntent.getService(
            this,
            1,
            disarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("TouchGuard Protection")
            .setContentText(statusText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "Disarm", disarmPendingIntent)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        disarm()
        serviceScope.cancel()
        toneGenerator?.release()
    }

    companion object {
        const val CHANNEL_ID = "touchguard_motion_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_PROTECTION = "com.example.touchguard.ACTION_START"
        const val ACTION_DISARM = "com.example.touchguard.ACTION_DISARM"
        const val ACTION_STOP_SERVICE = "com.example.touchguard.ACTION_STOP"

        private val _protectionState = MutableStateFlow(ServiceProtectionState.DISARMED)
        val protectionState: StateFlow<ServiceProtectionState> = _protectionState.asStateFlow()

        private val _armingCountdown = MutableStateFlow(0)
        val armingCountdown: StateFlow<Int> = _armingCountdown.asStateFlow()

        private val _liveSensorData = MutableStateFlow(LiveSensorData())
        val liveSensorData: StateFlow<LiveSensorData> = _liveSensorData.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, MotionProtectionService::class.java).apply {
                action = ACTION_START_PROTECTION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun disarm(context: Context) {
            val intent = Intent(context, MotionProtectionService::class.java).apply {
                action = ACTION_DISARM
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MotionProtectionService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
