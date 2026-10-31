package com.example.ui

import android.Manifest
import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TopSafetyBar
import com.example.ui.screens.EmergencyTakeoverScreen
import com.example.ui.screens.HealthScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.VeilCyanAccent
import com.example.ui.theme.VeilEmerald
import com.example.ui.theme.VeilEmergencyBright
import com.example.ui.theme.VeilEmergencyRed

enum class VeilTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    EMERGENCY("Emergency", Icons.Filled.CrisisAlert, Icons.Outlined.CrisisAlert),
    HEALTH("Health", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    viewModel: VeilViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val connectionState by viewModel.connectionState.collectAsState()
    val batteryPercent by viewModel.batteryPercent.collectAsState()
    val heartRateBpm by viewModel.heartRateBpm.collectAsState()
    val heartRateHistory by viewModel.heartRateHistory.collectAsState()
    val isSimulated by viewModel.isSimulated.collectAsState()
    val activeAlert by viewModel.activeAlert.collectAsState()
    val lastAlertTime by viewModel.lastAlertTime.collectAsState()
    val alertsHistory by viewModel.alertsHistory.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    var selectedTab by remember { mutableStateOf(VeilTab.HOME) }
    var showScannerSheet by remember { mutableStateOf(false) }

    // Keep screen awake while connected or while an alert is active
    val isConnected = connectionState.isConnected
    val isAlertActive = activeAlert != null

    DisposableEffect(isConnected, isAlertActive) {
        if (isConnected || isAlertActive) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Permission launcher for BLE, Location, and SMS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions updated
    }

    fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    LaunchedEffect(Unit) {
        requestRequiredPermissions()
    }

    // Handle back press
    BackHandler(enabled = selectedTab != VeilTab.HOME || isAlertActive) {
        if (isAlertActive) {
            viewModel.dismissAlert()
        } else {
            selectedTab = VeilTab.HOME
        }
    }

    // If an alert is active, it opens as a full-screen red takeover whatever tab the user is on
    if (activeAlert != null) {
        EmergencyTakeoverScreen(
            alertState = activeAlert,
            emergencyContacts = emergencyContacts,
            userName = userName,
            onDismissAlert = { viewModel.dismissAlert() },
            onSendSmsAll = { viewModel.sendSmsToContacts(context, activeAlert?.location) },
            onCallContact = { phone -> viewModel.callContact(context, phone) },
            onWhatsAppContact = { phone -> viewModel.sendWhatsApp(context, phone, activeAlert?.location) }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopSafetyBar(
                    isSimulated = isSimulated,
                    batteryPercent = batteryPercent,
                    isConnected = isConnected,
                    isAlertActive = isAlertActive
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .height(72.dp)
                        .testTag("veil_bottom_navigation"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    VeilTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                            icon = {
                                if (tab == VeilTab.EMERGENCY) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) VeilEmergencyRed else MaterialTheme.colorScheme.tertiary
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) VeilEmerald else MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = when {
                                        isSelected && tab == VeilTab.EMERGENCY -> VeilEmergencyRed
                                        isSelected -> VeilEmerald
                                        else -> MaterialTheme.colorScheme.tertiary
                                    }
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = when {
                                    tab == VeilTab.EMERGENCY -> VeilEmergencyRed.copy(alpha = 0.2f)
                                    else -> VeilEmerald.copy(alpha = 0.15f)
                                }
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    VeilTab.HOME -> {
                        HomeScreen(
                            connectionState = connectionState,
                            batteryPercent = batteryPercent,
                            lastAlertTime = lastAlertTime,
                            isAlertActive = isAlertActive,
                            isScanning = isScanning,
                            discoveredDevices = discoveredDevices,
                            onStartScan = {
                                requestRequiredPermissions()
                                viewModel.startScanning()
                            },
                            onStopScan = { viewModel.stopScanning() },
                            onConnectDiscoveredDevice = { device ->
                                viewModel.connectToDiscoveredDevice(device)
                            },
                            onConnectWristband = {
                                requestRequiredPermissions()
                                viewModel.scanAndConnect()
                            },
                            onDisconnect = { viewModel.disconnect() },
                            onActivateSos = { viewModel.triggerSosAlert() },
                            onSendTestAlert = { viewModel.triggerTestAlert() },
                            onSimulateWristbandPress = { viewModel.triggerWristbandSimulatedAlert() }
                        )
                    }

                    VeilTab.EMERGENCY -> {
                        // Emergency screen when accessed directly via bottom tabs
                        EmergencyTakeoverScreen(
                            alertState = activeAlert,
                            emergencyContacts = emergencyContacts,
                            userName = userName,
                            onDismissAlert = {
                                viewModel.dismissAlert()
                                selectedTab = VeilTab.HOME
                            },
                            onSendSmsAll = { viewModel.sendSmsToContacts(context, activeAlert?.location) },
                            onCallContact = { phone -> viewModel.callContact(context, phone) },
                            onWhatsAppContact = { phone -> viewModel.sendWhatsApp(context, phone, activeAlert?.location) }
                        )
                    }

                    VeilTab.HEALTH -> {
                        HealthScreen(
                            heartRateBpm = heartRateBpm,
                            heartRateHistory = heartRateHistory,
                            isConnected = isConnected,
                            isSimulated = isSimulated,
                            onToggleSimulatedHr = { viewModel.toggleSimulatedHeartRate(it) }
                        )
                    }

                    VeilTab.HISTORY -> {
                        HistoryScreen(
                            alerts = alertsHistory,
                            onClearHistory = { viewModel.clearAlertHistory() }
                        )
                    }

                    VeilTab.SETTINGS -> {
                        SettingsScreen(
                            userName = userName,
                            emergencyContacts = emergencyContacts,
                            isSimulated = isSimulated,
                            onUpdateUserName = { viewModel.updateUserName(it) },
                            onAddContact = { n, p, r -> viewModel.addContact(n, p, r) },
                            onUpdateContact = { viewModel.updateContact(it) },
                            onDeleteContact = { viewModel.deleteContact(it) },
                            onToggleSimulation = { viewModel.setSimulationMode(it) },
                            onRequestPermissions = { requestRequiredPermissions() },
                            onOpenScanner = {
                                requestRequiredPermissions()
                                viewModel.startScanning()
                                showScannerSheet = true
                            }
                        )
                    }
                }
            }

            if (showScannerSheet) {
                com.example.ui.components.BleScannerSheet(
                    isScanning = isScanning,
                    discoveredDevices = discoveredDevices,
                    connectionState = connectionState,
                    onStartScan = {
                        requestRequiredPermissions()
                        viewModel.startScanning()
                    },
                    onStopScan = { viewModel.stopScanning() },
                    onConnectDevice = { device ->
                        viewModel.connectToDiscoveredDevice(device)
                        showScannerSheet = false
                    },
                    onDismiss = { showScannerSheet = false }
                )
            }
        }
    }
}
