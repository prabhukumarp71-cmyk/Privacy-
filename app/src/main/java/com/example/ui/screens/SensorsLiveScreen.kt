package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProtectionSettings
import com.example.data.model.SensitivityLevel
import com.example.service.LiveSensorData
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

@Composable
fun SensorsLiveScreen(
    sensorData: LiveSensorData,
    settings: ProtectionSettings,
    onSelectSensitivity: (SensitivityLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isTriggered = sensorData.deltaAcc >= settings.sensitivity.threshold

    val statusColor by animateColorAsState(
        targetValue = if (isTriggered) AlertRed else if (sensorData.deltaAcc > settings.sensitivity.threshold * 0.6f) WarningAmber else SafeGreen,
        label = "statusColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Motion & Sensor Lab",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Calibrate and test touch & pick-up sensitivity in real time",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Sensitivity Preset Selector
        Text(
            text = "TEST SENSITIVITY PRESETS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SensitivityLevel.values().forEach { level ->
                FilterChip(
                    selected = settings.sensitivity == level,
                    onClick = { onSelectSensitivity(level) },
                    label = { Text(level.title.split(" ")[0], fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = Color(0xFF031E2F)
                    ),
                    modifier = Modifier.weight(1f).testTag("sensitivity_chip_${level.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger Status Banner
        Surface(
            color = statusColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, statusColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTriggered) Icons.Default.TouchApp else Icons.Default.Speed,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (isTriggered) "TRIGGER THRESHOLD CROSSED!" else "IDLE / BELOW THRESHOLD",
                        color = statusColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isTriggered)
                            "Motion would immediately close active apps and turn screen off"
                        else
                            "Gently tap or lift the phone to test detection",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive 2D Tilt & Orientation Visualizer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tilt_bubble_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate700)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pick-Up & Tilt Bubble Level",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Visualizes phone inclination and movement angle",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .border(2.dp, Slate700, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val maxRadius = size.width / 2f - 18.dp.toPx()

                        // Crosshair rings
                        drawCircle(
                            color = Slate700,
                            radius = maxRadius * 0.5f,
                            center = Offset(centerX, centerY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                        drawLine(
                            color = Slate700,
                            start = Offset(centerX, 0f),
                            end = Offset(centerX, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Slate700,
                            start = Offset(0f, centerY),
                            end = Offset(size.width, centerY),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Bubble position mapped from Acc X and Y
                        val bubbleX = (centerX - (sensorData.accX / 9.8f) * maxRadius).coerceIn(18.dp.toPx(), size.width - 18.dp.toPx())
                        val bubbleY = (centerY + (sensorData.accY / 9.8f) * maxRadius).coerceIn(18.dp.toPx(), size.height - 18.dp.toPx())

                        drawCircle(
                            color = statusColor,
                            radius = 14.dp.toPx(),
                            center = Offset(bubbleX, bubbleY)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Numerical Sensor Data Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate700)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Raw Telemetry",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SensorValueRow(
                    label = "Delta Acceleration (Spike)",
                    value = String.format("%.2f m/s²", sensorData.deltaAcc),
                    threshold = String.format("Threshold: %.2f", settings.sensitivity.threshold),
                    progress = (sensorData.deltaAcc / (settings.sensitivity.threshold * 1.5f)).coerceIn(0f, 1f),
                    barColor = statusColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                SensorValueRow(
                    label = "Angular Rotation (Gyro)",
                    value = String.format("%.2f rad/s", sensorData.gyroSpeed),
                    threshold = "Threshold: 1.80 rad/s",
                    progress = (sensorData.gyroSpeed / 3.0f).coerceIn(0f, 1f),
                    barColor = NeonBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Pocket / Proximity Sensor", color = TextPrimary, fontSize = 12.sp)
                        Text(
                            text = if (sensorData.proximityDistance < 0) "Unavailable or Idle"
                            else if (sensorData.isNear) "Covered / In Pocket" else "Open Air / Table",
                            color = if (sensorData.isNear) WarningAmber else SafeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = if (sensorData.isNear) WarningAmber.copy(alpha = 0.2f) else SafeGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (sensorData.isNear) "NEAR" else "FAR",
                            color = if (sensorData.isNear) WarningAmber else SafeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SensorValueRow(
    label: String,
    value: String,
    threshold: String,
    progress: Float,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextPrimary, fontSize = 12.sp)
            Text(value, color = barColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Slate800
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            threshold,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
