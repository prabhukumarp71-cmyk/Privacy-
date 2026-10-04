package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.model.IncidentEntity
import com.example.data.model.ProtectionSettings
import com.example.data.model.SensitivityLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProtectionRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val incidentDao = db.incidentDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("touchguard_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ProtectionSettings> = _settings.asStateFlow()

    val allIncidents: Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()
    val incidentCount: Flow<Int> = incidentDao.getCount()

    private fun loadSettings(): ProtectionSettings {
        val sensitivityStr = prefs.getString("sensitivity", SensitivityLevel.MEDIUM.name) ?: SensitivityLevel.MEDIUM.name
        val sensitivity = try {
            SensitivityLevel.valueOf(sensitivityStr)
        } catch (_: Exception) {
            SensitivityLevel.MEDIUM
        }

        return ProtectionSettings(
            isArmed = prefs.getBoolean("is_armed", false),
            sensitivity = sensitivity,
            armingDelaySeconds = prefs.getInt("arming_delay", 5),
            actionCloseAppReturnHome = prefs.getBoolean("action_home", true),
            actionLockScreenOff = prefs.getBoolean("action_lock", true),
            actionPrivacyBlackout = prefs.getBoolean("action_blackout", true),
            actionAlarmSound = prefs.getBoolean("action_alarm", false),
            actionVibration = prefs.getBoolean("action_vibration", true),
            pocketProximityMode = prefs.getBoolean("pocket_mode", true),
            disarmPin = prefs.getString("disarm_pin", "") ?: ""
        )
    }

    fun updateSettings(transform: (ProtectionSettings) -> ProtectionSettings) {
        val newSettings = transform(_settings.value)
        _settings.value = newSettings

        prefs.edit().apply {
            putBoolean("is_armed", newSettings.isArmed)
            putString("sensitivity", newSettings.sensitivity.name)
            putInt("arming_delay", newSettings.armingDelaySeconds)
            putBoolean("action_home", newSettings.actionCloseAppReturnHome)
            putBoolean("action_lock", newSettings.actionLockScreenOff)
            putBoolean("action_blackout", newSettings.actionPrivacyBlackout)
            putBoolean("action_alarm", newSettings.actionAlarmSound)
            putBoolean("action_vibration", newSettings.actionVibration)
            putBoolean("pocket_mode", newSettings.pocketProximityMode)
            putString("disarm_pin", newSettings.disarmPin)
            apply()
        }
    }

    suspend fun recordIncident(
        reason: String,
        sensorReading: String,
        actionTaken: String,
        alarmTriggered: Boolean
    ): Long {
        val incident = IncidentEntity(
            triggerReason = reason,
            sensorReading = sensorReading,
            actionTaken = actionTaken,
            alarmTriggered = alarmTriggered
        )
        return incidentDao.insertIncident(incident)
    }

    suspend fun clearHistory() {
        incidentDao.clearAll()
    }

    suspend fun deleteIncident(id: Long) {
        incidentDao.deleteById(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: ProtectionRepository? = null

        fun getInstance(context: Context): ProtectionRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ProtectionRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
