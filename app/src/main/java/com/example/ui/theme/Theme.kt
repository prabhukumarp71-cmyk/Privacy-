package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF031E2F),
    primaryContainer = DarkNavy,
    onPrimaryContainer = Color(0xFFC7E8FF),
    secondary = NeonBlue,
    onSecondary = Color.White,
    secondaryContainer = Slate700,
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = WarningAmber,
    onTertiary = Color(0xFF452B00),
    error = AlertRed,
    onError = Color.White,
    background = Slate950,
    onBackground = TextPrimary,
    surface = Slate900,
    onSurface = TextPrimary,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate600
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
