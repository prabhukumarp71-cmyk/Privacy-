package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin.TouchGuardDeviceAdminReceiver
import com.example.data.model.IncidentEntity
import com.example.data.model.ProtectionSettings
import com.example.data.model.SensitivityLevel
import com.example.data.repository.ProtectionRepository
import com.example.service.LiveSensorData
import com.example.service.MotionProtectionService
import com.example.service.ServiceProtectionState
import com.example.service.TouchGuardAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PermissionStatus(
    val isAccessibilityEnabled: Boolean = false,
    val isDeviceAdminEnabled: Boolean = false,
    val isNotificationEnabled: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProtectionRepository.getInstance(application)

    val settings: StateFlow<ProtectionSettings> = repository.settings
    val serviceState: StateFlow<ServiceProtectionState> = MotionProtectionService.protectionState
    val armingCountdown: StateFlow<Int> = MotionProtectionService.armingCountdown
    val liveSensorData: StateFlow<LiveSensorData> = MotionProtectionService.liveSensorData

    val incidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidentCount: StateFlow<Int> = repository.incidentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _permissions = MutableStateFlow(PermissionStatus())
    val permissions: StateFlow<PermissionStatus> = _permissions.asStateFlow()

    init {
        refreshPermissionStatus()
    }

    fun refreshPermissionStatus() {
        val context = getApplication<Application>()
        val a11y = TouchGuardAccessibilityService.isAccessibilitySettingsEnabled(context)
        val admin = TouchGuardDeviceAdminReceiver.isAdminActive(context)

        _permissions.value = PermissionStatus(
            isAccessibilityEnabled = a11y,
            isDeviceAdminEnabled = admin,
            isNotificationEnabled = true
        )
    }

    fun toggleProtection(context: Context) {
        val currentState = serviceState.value
        if (currentState == ServiceProtectionState.ARMED || currentState == ServiceProtectionState.ARMING) {
            MotionProtectionService.disarm(context)
        } else {
            MotionProtectionService.start(context)
        }
    }

    fun setSensitivity(level: SensitivityLevel) {
        repository.updateSettings { it.copy(sensitivity = level) }
    }

    fun setArmingDelay(delaySec: Int) {
        repository.updateSettings { it.copy(armingDelaySeconds = delaySec) }
    }

    fun toggleActionHome(enabled: Boolean) {
        repository.updateSettings { it.copy(actionCloseAppReturnHome = enabled) }
    }

    fun toggleActionLock(enabled: Boolean) {
        repository.updateSettings { it.copy(actionLockScreenOff = enabled) }
    }

    fun toggleActionBlackout(enabled: Boolean) {
        repository.updateSettings { it.copy(actionPrivacyBlackout = enabled) }
    }

    fun toggleActionAlarm(enabled: Boolean) {
        repository.updateSettings { it.copy(actionAlarmSound = enabled) }
    }

    fun toggleActionVibration(enabled: Boolean) {
        repository.updateSettings { it.copy(actionVibration = enabled) }
    }

    fun togglePocketMode(enabled: Boolean) {
        repository.updateSettings { it.copy(pocketProximityMode = enabled) }
    }

    fun setDisarmPin(pin: String) {
        repository.updateSettings { it.copy(disarmPin = pin) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteIncident(id: Long) {
        viewModelScope.launch {
            repository.deleteIncident(id)
        }
    }
}
