package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TouchGuardAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceBound.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event inspection needed - service is primarily used for global actions
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isServiceBound.value = false
        }
    }

    fun closeActiveAppAndReturnHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun turnScreenOffAndLock(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    companion object {
        var instance: TouchGuardAccessibilityService? = null
            private set

        private val _isServiceBound = MutableStateFlow(false)
        val isServiceBound: StateFlow<Boolean> = _isServiceBound

        fun isAccessibilitySettingsEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${TouchGuardAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = enabledServices.split(":")
            for (service in colonSplitter) {
                if (service.equals(expectedServiceName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
