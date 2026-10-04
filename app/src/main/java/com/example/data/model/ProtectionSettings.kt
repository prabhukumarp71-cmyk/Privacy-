package com.example.data.model

enum class SensitivityLevel(val title: String, val threshold: Float, val description: String) {
    LOW("Low (Lift/Pick up)", 4.0f, "Triggers when firmly picked up or shaken"),
    MEDIUM("Medium (Standard)", 2.2f, "Triggers on normal lift or movement"),
    HIGH("High (Light Touch)", 1.2f, "Triggers on gentle touch or desk tap"),
    EXTREME("Extreme (Vibration)", 0.6f, "Ultra-sensitive to any microscopic touch")
}

data class ProtectionSettings(
    val isArmed: Boolean = false,
    val sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM,
    val armingDelaySeconds: Int = 5,
    val actionCloseAppReturnHome: Boolean = true,
    val actionLockScreenOff: Boolean = true,
    val actionPrivacyBlackout: Boolean = true,
    val actionAlarmSound: Boolean = false,
    val actionVibration: Boolean = true,
    val pocketProximityMode: Boolean = true,
    val disarmPin: String = ""
)
