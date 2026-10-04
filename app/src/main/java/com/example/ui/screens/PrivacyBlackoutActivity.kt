package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.repository.ProtectionRepository
import com.example.service.MotionProtectionService

class PrivacyBlackoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen dim or blackout immediately
        val windowParams = window.attributes
        windowParams.screenBrightness = 0.01f
        window.attributes = windowParams

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val triggerReason = intent.getStringExtra("EXTRA_REASON") ?: "Motion / Touch Detected"
        val repository = ProtectionRepository.getInstance(this)
        val settings = repository.settings.value

        setContent {
            BlackoutScreen(
                reason = triggerReason,
                requiredPin = settings.disarmPin,
                onDismiss = {
                    returnToHomeScreen(this)
                    finish()
                },
                onOpenApp = {
                    val mainIntent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(mainIntent)
                    finish()
                }
            )
        }
    }

    companion object {
        fun returnToHomeScreen(context: Context) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(homeIntent)
        }
    }
}

@Composable
fun BlackoutScreen(
    reason: String,
    requiredPin: String,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showUnlockUi by remember { mutableStateOf(requiredPin.isEmpty()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (requiredPin.isNotEmpty()) {
                    showUnlockUi = true
                }
            }
            .padding(24.dp)
            .testTag("blackout_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "Screen Shield Active",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TOUCHGUARD SHIELD",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Apps Closed & Screen Blanked",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFF7F1D1D).copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.5f))
            ) {
                Text(
                    text = reason,
                    color = Color(0xFFFCA5A5),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (requiredPin.isNotEmpty() && !showUnlockUi) {
                Text(
                    text = "Tap screen to enter PIN",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }

            AnimatedVisibility(visible = showUnlockUi || requiredPin.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (requiredPin.isNotEmpty()) {
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = {
                                enteredPin = it
                                pinError = false
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            label = { Text("Enter Disarm PIN", color = Color(0xFF94A3B8)) },
                            isError = pinError,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .testTag("disarm_pin_input")
                        )

                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Incorrect PIN",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Button(
                            onClick = {
                                if (requiredPin.isEmpty() || enteredPin == requiredPin) {
                                    onDismiss()
                                } else {
                                    pinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("return_home_button")
                        ) {
                            Text("Go to Home", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (requiredPin.isEmpty() || enteredPin == requiredPin) {
                                    onOpenApp()
                                } else {
                                    pinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_touchguard_button")
                        ) {
                            Text("Open App", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
