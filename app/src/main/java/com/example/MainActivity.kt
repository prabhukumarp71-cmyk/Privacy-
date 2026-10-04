package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.admin.TouchGuardDeviceAdminReceiver
import com.example.service.MotionProtectionService
import com.example.service.ServiceProtectionState
import com.example.service.TouchGuardAccessibilityService
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.SensorsLiveScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.refreshPermissionStatus()
    }

    private val deviceAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.refreshPermissionStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MyApplicationTheme {
                TouchGuardApp(
                    viewModel = viewModel,
                    onRequestAccessibility = {
                        TouchGuardAccessibilityService.openAccessibilitySettings(this)
                    },
                    onRequestDeviceAdmin = {
                        val intent = TouchGuardDeviceAdminReceiver.createEnableAdminIntent(this)
                        deviceAdminLauncher.launch(intent)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissionStatus()
    }
}

@Composable
fun TouchGuardApp(
    viewModel: MainViewModel,
    onRequestAccessibility: () -> Unit,
    onRequestDeviceAdmin: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val serviceState by viewModel.serviceState.collectAsStateWithLifecycle()
    val armingCountdown by viewModel.armingCountdown.collectAsStateWithLifecycle()
    val liveSensorData by viewModel.liveSensorData.collectAsStateWithLifecycle()
    val incidents by viewModel.incidents.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    var showPinDialogToDisarm by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val handleToggleProtection = {
        if ((serviceState == ServiceProtectionState.ARMED || serviceState == ServiceProtectionState.ARMING)
            && settings.disarmPin.isNotEmpty()
        ) {
            enteredPin = ""
            pinError = false
            showPinDialogToDisarm = true
        } else {
            viewModel.toggleProtection(context)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                contentColor = TextPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Security else Icons.Outlined.Security,
                            contentDescription = "Shield"
                        )
                    },
                    label = { Text("Shield", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF031E2F),
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_shield")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.Sensors else Icons.Outlined.Sensors,
                            contentDescription = "Sensor Lab"
                        )
                    },
                    label = { Text("Sensors", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF031E2F),
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_sensors")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            if (selectedTab == 2) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("Logs", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF031E2F),
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_logs")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF031E2F),
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        },
        containerColor = Slate950
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    context = context,
                    serviceState = serviceState,
                    armingCountdown = armingCountdown,
                    settings = settings,
                    sensorData = liveSensorData,
                    permissions = permissions,
                    onToggleProtection = handleToggleProtection,
                    onRequestAccessibility = onRequestAccessibility,
                    onRequestDeviceAdmin = onRequestDeviceAdmin,
                    onNavigateToSensors = { selectedTab = 1 },
                    onNavigateToSettings = { selectedTab = 3 }
                )
                1 -> SensorsLiveScreen(
                    sensorData = liveSensorData,
                    settings = settings,
                    onSelectSensitivity = { viewModel.setSensitivity(it) }
                )
                2 -> LogsScreen(
                    incidents = incidents,
                    onClearAll = { viewModel.clearHistory() },
                    onDeleteIncident = { viewModel.deleteIncident(it) }
                )
                3 -> SettingsScreen(
                    settings = settings,
                    permissions = permissions,
                    onSensitivityChanged = { viewModel.setSensitivity(it) },
                    onArmingDelayChanged = { viewModel.setArmingDelay(it) },
                    onToggleActionHome = { viewModel.toggleActionHome(it) },
                    onToggleActionLock = { viewModel.toggleActionLock(it) },
                    onToggleActionBlackout = { viewModel.toggleActionBlackout(it) },
                    onToggleActionAlarm = { viewModel.toggleActionAlarm(it) },
                    onToggleActionVibration = { viewModel.toggleActionVibration(it) },
                    onTogglePocketMode = { viewModel.togglePocketMode(it) },
                    onSetDisarmPin = { viewModel.setDisarmPin(it) },
                    onRequestAccessibility = onRequestAccessibility,
                    onRequestDeviceAdmin = onRequestDeviceAdmin
                )
            }
        }
    }

    if (showPinDialogToDisarm) {
        AlertDialog(
            onDismissRequest = { showPinDialogToDisarm = false },
            title = { Text("Enter PIN to Disarm") },
            text = {
                Column {
                    Text("Protection is locked. Enter your 4-digit PIN to disarm.", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("4-Digit PIN") },
                        isError = pinError,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("disarm_dialog_pin_input")
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Incorrect PIN. Please try again.", color = AlertRed, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == settings.disarmPin) {
                            showPinDialogToDisarm = false
                            viewModel.toggleProtection(context)
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Disarm", color = Color(0xFF031E2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialogToDisarm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Slate900,
            titleContentColor = TextPrimary
        )
    }
}
