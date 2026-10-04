package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProtectionSettings
import com.example.data.model.SensitivityLevel
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.PermissionStatus

@Composable
fun SettingsScreen(
    settings: ProtectionSettings,
    permissions: PermissionStatus,
    onSensitivityChanged: (SensitivityLevel) -> Unit,
    onArmingDelayChanged: (Int) -> Unit,
    onToggleActionHome: (Boolean) -> Unit,
    onToggleActionLock: (Boolean) -> Unit,
    onToggleActionBlackout: (Boolean) -> Unit,
    onToggleActionAlarm: (Boolean) -> Unit,
    onToggleActionVibration: (Boolean) -> Unit,
    onTogglePocketMode: (Boolean) -> Unit,
    onSetDisarmPin: (String) -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestDeviceAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var pinInput by remember(settings.disarmPin) { mutableStateOf(settings.disarmPin) }
    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Protection Settings",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure trigger reactions and system privileges",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Trigger Responses Section
        SectionHeader("TRIGGER RESPONSES (WHEN TOUCHED / PICKED UP)")

        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Default.Home,
                title = "Close Active App & Go Home",
                description = "Instantly minimizes running foreground apps and returns to launcher",
                checked = settings.actionCloseAppReturnHome,
                onCheckedChange = onToggleActionHome,
                testTag = "toggle_action_home"
            )

            SettingsDivider()

            SettingsToggleRow(
                icon = Icons.Default.Lock,
                title = "Turn Screen Off & Lock Device",
                description = "Locks phone instantly using Accessibility or Device Admin",
                checked = settings.actionLockScreenOff,
                onCheckedChange = onToggleActionLock,
                testTag = "toggle_action_lock"
            )

            SettingsDivider()

            SettingsToggleRow(
                icon = Icons.Default.VisibilityOff,
                title = "Instant Privacy Blackout Shield",
                description = "Immediately blacks out display to conceal active contents",
                checked = settings.actionPrivacyBlackout,
                onCheckedChange = onToggleActionBlackout,
                testTag = "toggle_action_blackout"
            )

            SettingsDivider()

            SettingsToggleRow(
                icon = Icons.Default.Vibration,
                title = "Vibration Alert",
                description = "Tactile vibration pulse when unauthorized touch is sensed",
                checked = settings.actionVibration,
                onCheckedChange = onToggleActionVibration,
                testTag = "toggle_action_vibration"
            )

            SettingsDivider()

            SettingsToggleRow(
                icon = Icons.Default.VolumeUp,
                title = "Sound Siren Alarm",
                description = "Audible alarm tone plays when triggered",
                checked = settings.actionAlarmSound,
                onCheckedChange = onToggleActionAlarm,
                testTag = "toggle_action_alarm"
            )

            SettingsDivider()

            SettingsToggleRow(
                icon = Icons.Default.Sensors,
                title = "Pocket & Proximity Trigger",
                description = "Detects when phone is drawn from pocket or picked off a surface",
                checked = settings.pocketProximityMode,
                onCheckedChange = onTogglePocketMode,
                testTag = "toggle_pocket_mode"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sensitivity Level Section
        SectionHeader("MOTION SENSITIVITY")

        SettingsCard {
            SensitivityLevel.values().forEachIndexed { index, level ->
                if (index > 0) SettingsDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSensitivityChanged(level) }
                        .padding(vertical = 12.dp)
                        .testTag("select_sensitivity_${level.name}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = level.title,
                            color = if (settings.sensitivity == level) ElectricCyan else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (settings.sensitivity == level) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = level.description,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (settings.sensitivity == level) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF031E2F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Arming Delay Section
        SectionHeader("ARMING GRACE PERIOD")

        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Countdown Before Arming",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${settings.armingDelaySeconds}s delay",
                    color = ElectricCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = settings.armingDelaySeconds.toFloat(),
                onValueChange = { onArmingDelayChanged(it.toInt()) },
                valueRange = 0f..15f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = ElectricCyan,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = Slate800
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("arming_delay_slider")
            )

            Text(
                text = "Gives you time to place your phone flat or slide it into your pocket before active protection arms.",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Disarm PIN Protection Section
        SectionHeader("SECURITY & DISARM PIN")

        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PIN to Disarm",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (settings.disarmPin.isEmpty())
                            "No PIN set • Anyone can tap Disarm"
                        else
                            "PIN protection active • Disarming requires PIN",
                        color = if (settings.disarmPin.isEmpty()) TextSecondary else SafeGreen,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showPinDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.disarmPin.isEmpty()) Slate800 else ElectricCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("setup_pin_button")
                ) {
                    Text(
                        text = if (settings.disarmPin.isEmpty()) "Set PIN" else "Change",
                        color = if (settings.disarmPin.isEmpty()) TextPrimary else Color(0xFF031E2F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (settings.disarmPin.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onSetDisarmPin("") },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Remove PIN Protection", color = WarningAmber, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Permissions Shortcuts
        SectionHeader("SYSTEM PERMISSIONS")

        SettingsCard {
            PermissionItem(
                title = "Accessibility Service",
                description = "Enables automatic closing of foreground apps and locking screen.",
                isGranted = permissions.isAccessibilityEnabled,
                buttonText = "Open Accessibility",
                onClick = onRequestAccessibility,
                testTag = "perm_accessibility_btn"
            )

            SettingsDivider()

            PermissionItem(
                title = "Device Administrator",
                description = "Allows instant hardware screen off & lock.",
                isGranted = permissions.isDeviceAdminEnabled,
                buttonText = "Activate Admin",
                onClick = onRequestDeviceAdmin,
                testTag = "perm_admin_btn"
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showPinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set Disarm PIN") },
            text = {
                Column {
                    Text("Enter a 4-digit numeric passcode to secure disarm actions.", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInput = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetDisarmPin(pinInput)
                        showPinDialog = false
                    },
                    enabled = pinInput.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Save PIN", color = Color(0xFF031E2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Slate900,
            titleContentColor = TextPrimary
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Slate800)
            .padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) ElectricCyan else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF031E2F),
                checkedTrackColor = ElectricCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Slate800
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (isGranted) SafeGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isGranted) "ACTIVE" else "NOT SET",
                        color = if (isGranted) SafeGreen else WarningAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        if (!isGranted) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag(testTag)
            ) {
                Text(buttonText, color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
