package com.example.ui.screens

import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.TouchGuardDeviceAdminReceiver
import com.example.data.model.ProtectionSettings
import com.example.data.model.SensitivityLevel
import com.example.service.LiveSensorData
import com.example.service.ServiceProtectionState
import com.example.service.TouchGuardAccessibilityService
import com.example.ui.theme.AlertRed
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
fun HomeScreen(
    context: Context,
    serviceState: ServiceProtectionState,
    armingCountdown: Int,
    settings: ProtectionSettings,
    sensorData: LiveSensorData,
    permissions: PermissionStatus,
    onToggleProtection: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestDeviceAdmin: () -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TouchGuard",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Anti-Touch App Closer & Screen Off",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = when (serviceState) {
                    ServiceProtectionState.ARMED -> SafeGreen.copy(alpha = 0.15f)
                    ServiceProtectionState.ARMING -> WarningAmber.copy(alpha = 0.15f)
                    ServiceProtectionState.TRIGGERED -> AlertRed.copy(alpha = 0.15f)
                    ServiceProtectionState.DISARMED -> Slate800
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    when (serviceState) {
                        ServiceProtectionState.ARMED -> SafeGreen
                        ServiceProtectionState.ARMING -> WarningAmber
                        ServiceProtectionState.TRIGGERED -> AlertRed
                        ServiceProtectionState.DISARMED -> Slate700
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (serviceState) {
                                    ServiceProtectionState.ARMED -> SafeGreen
                                    ServiceProtectionState.ARMING -> WarningAmber
                                    ServiceProtectionState.TRIGGERED -> AlertRed
                                    ServiceProtectionState.DISARMED -> TextSecondary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (serviceState) {
                            ServiceProtectionState.ARMED -> "ARMED"
                            ServiceProtectionState.ARMING -> "ARMING ($armingCountdown)"
                            ServiceProtectionState.TRIGGERED -> "TRIGGERED!"
                            ServiceProtectionState.DISARMED -> "DISARMED"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (serviceState) {
                            ServiceProtectionState.ARMED -> SafeGreen
                            ServiceProtectionState.ARMING -> WarningAmber
                            ServiceProtectionState.TRIGGERED -> AlertRed
                            ServiceProtectionState.DISARMED -> TextSecondary
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Center Animated Shield Button
        CenterShieldIndicator(
            serviceState = serviceState,
            armingCountdown = armingCountdown,
            onToggle = onToggleProtection
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = when (serviceState) {
                ServiceProtectionState.ARMED -> "Protection Active • Phone is Armed"
                ServiceProtectionState.ARMING -> "Place phone on flat surface or in pocket"
                ServiceProtectionState.TRIGGERED -> "Motion Detected! Closing Apps & Turning Screen Off"
                ServiceProtectionState.DISARMED -> "Tap shield to arm motion detection"
            },
            color = when (serviceState) {
                ServiceProtectionState.ARMED -> SafeGreen
                ServiceProtectionState.ARMING -> WarningAmber
                ServiceProtectionState.TRIGGERED -> AlertRed
                ServiceProtectionState.DISARMED -> TextSecondary
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Live Motion Monitor Bar
        LiveMotionGaugeCard(
            sensorData = sensorData,
            threshold = settings.sensitivity.threshold,
            onOpenTester = onNavigateToSensors
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Permissions & Readiness Status
        SystemReadinessCard(
            permissions = permissions,
            onRequestAccessibility = onRequestAccessibility,
            onRequestDeviceAdmin = onRequestDeviceAdmin
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Configured Actions Summary Card
        ActionsSummaryCard(
            settings = settings,
            onConfigure = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CenterShieldIndicator(
    serviceState: ServiceProtectionState,
    armingCountdown: Int,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (serviceState == ServiceProtectionState.ARMED) 1.08f else if (serviceState == ServiceProtectionState.TRIGGERED) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowColor by animateColorAsState(
        targetValue = when (serviceState) {
            ServiceProtectionState.ARMED -> SafeGreen
            ServiceProtectionState.ARMING -> WarningAmber
            ServiceProtectionState.TRIGGERED -> AlertRed
            ServiceProtectionState.DISARMED -> Slate700
        },
        label = "glow"
    )

    Box(
        modifier = Modifier
            .size(210.dp)
            .testTag("arm_shield_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = 0.12f))
                .border(2.dp, glowColor.copy(alpha = 0.4f), CircleShape)
        )

        // Middle ring
        Box(
            modifier = Modifier
                .size(165.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Slate800, Slate900)
                    )
                )
                .border(2.dp, glowColor, CircleShape)
                .clickable { onToggle() }
                .testTag("arm_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (serviceState == ServiceProtectionState.ARMING) {
                    Text(
                        text = "$armingCountdown",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = WarningAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ARMING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(
                        imageVector = when (serviceState) {
                            ServiceProtectionState.ARMED -> Icons.Default.Security
                            ServiceProtectionState.TRIGGERED -> Icons.Default.Warning
                            ServiceProtectionState.DISARMED -> Icons.Default.PowerSettingsNew
                            else -> Icons.Default.Security
                        },
                        contentDescription = "Arm Switch",
                        tint = glowColor,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when (serviceState) {
                            ServiceProtectionState.ARMED -> "DISARM"
                            ServiceProtectionState.TRIGGERED -> "STOP"
                            ServiceProtectionState.DISARMED -> "ARM NOW"
                            else -> "ARM"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun LiveMotionGaugeCard(
    sensorData: LiveSensorData,
    threshold: Float,
    onOpenTester: () -> Unit
) {
    val progress = (sensorData.deltaAcc / (threshold * 1.5f)).coerceIn(0f, 1f)
    val isNearThreshold = sensorData.deltaAcc >= threshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTester() }
            .testTag("motion_gauge_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, if (isNearThreshold) AlertRed else Slate700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Motion Sensor",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Motion Sensitivity",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = String.format("%.2f / %.2f m/s²", sensorData.deltaAcc, threshold),
                    color = if (isNearThreshold) AlertRed else TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isNearThreshold) AlertRed else if (progress > 0.6f) WarningAmber else ElectricCyan,
                trackColor = Slate800
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isNearThreshold) "⚠️ Trigger threshold exceeded!" else "Normal resting state",
                    color = if (isNearThreshold) AlertRed else TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Tap to test sensors →",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SystemReadinessCard(
    permissions: PermissionStatus,
    onRequestAccessibility: () -> Unit,
    onRequestDeviceAdmin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("system_readiness_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "System Action Capabilities",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Accessibility Status
            StatusRow(
                title = "Accessibility Service",
                description = "Required to close foreground apps & lock screen automatically",
                isActive = permissions.isAccessibilityEnabled,
                actionLabel = "Enable",
                onClick = onRequestAccessibility
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Device Admin Status
            StatusRow(
                title = "Device Admin (Optional)",
                description = "Direct hardware screen off & instant lock",
                isActive = permissions.isDeviceAdminEnabled,
                actionLabel = "Activate",
                onClick = onRequestDeviceAdmin
            )
        }
    }
}

@Composable
fun StatusRow(
    title: String,
    description: String,
    isActive: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isActive) SafeGreen else WarningAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        if (!isActive) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("enable_${title.lowercase().replace(" ", "_")}")
            ) {
                Text(actionLabel, color = Color(0xFF031E2F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActionsSummaryCard(
    settings: ProtectionSettings,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConfigure() }
            .testTag("actions_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Triggers & Actions When Picked Up",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure →",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionPill(
                icon = Icons.Default.Home,
                text = "Close Running App & Return to Home",
                enabled = settings.actionCloseAppReturnHome
            )

            Spacer(modifier = Modifier.height(6.dp))

            ActionPill(
                icon = Icons.Default.Lock,
                text = "Turn Screen Off & Lock Device",
                enabled = settings.actionLockScreenOff
            )

            Spacer(modifier = Modifier.height(6.dp))

            ActionPill(
                icon = Icons.Default.VisibilityOff,
                text = "Privacy Blackout Shield",
                enabled = settings.actionPrivacyBlackout
            )

            Spacer(modifier = Modifier.height(6.dp))

            ActionPill(
                icon = Icons.Default.TouchApp,
                text = "Vibration Alert",
                enabled = settings.actionVibration
            )
        }
    }
}

@Composable
fun ActionPill(
    icon: ImageVector,
    text: String,
    enabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) SafeGreen else TextSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}
