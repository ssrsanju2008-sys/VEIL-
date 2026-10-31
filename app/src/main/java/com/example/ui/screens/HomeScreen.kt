package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.BleConnectionState
import com.example.ble.DiscoveredBleDevice
import com.example.ui.components.BleScannerSheet
import com.example.ui.components.StatusRing
import com.example.ui.theme.VeilCyanAccent
import com.example.ui.theme.VeilEmerald
import com.example.ui.theme.VeilEmeraldBright
import com.example.ui.theme.VeilEmeraldContainer
import com.example.ui.theme.VeilEmeraldDark
import com.example.ui.theme.VeilEmergencyRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    connectionState: BleConnectionState,
    batteryPercent: Int?,
    lastAlertTime: Long?,
    isAlertActive: Boolean,
    isScanning: Boolean,
    discoveredDevices: List<DiscoveredBleDevice>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDiscoveredDevice: (DiscoveredBleDevice) -> Unit,
    onConnectWristband: () -> Unit,
    onDisconnect: () -> Unit,
    onActivateSos: () -> Unit,
    onSendTestAlert: () -> Unit,
    onSimulateWristbandPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState.isConnected
    val isConnecting = connectionState is BleConnectionState.Connecting

    val scrollState = rememberScrollState()
    var showScannerSheet by remember { mutableStateOf(false) }

    if (showScannerSheet) {
        BleScannerSheet(
            isScanning = isScanning,
            discoveredDevices = discoveredDevices,
            connectionState = connectionState,
            onStartScan = onStartScan,
            onStopScan = onStopScan,
            onConnectDevice = { device ->
                onConnectDiscoveredDevice(device)
                showScannerSheet = false
            },
            onDismiss = { showScannerSheet = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Status Ring Section
        Spacer(modifier = Modifier.height(10.dp))
        StatusRing(
            isConnected = isConnected,
            isAlertActive = isAlertActive
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Connection Error Notice if any
        if (connectionState is BleConnectionState.Error) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = connectionState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Telemetry & Status Grid Cards
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("system_telemetry_card"),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SYSTEM TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Row 1: Device State & System State
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Device status item
                    TelemetryItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bluetooth,
                        label = "Device",
                        value = when (connectionState) {
                            is BleConnectionState.Connected -> "Connected"
                            is BleConnectionState.Connecting -> "Connecting..."
                            is BleConnectionState.Scanning -> "Scanning..."
                            else -> "Disconnected"
                        },
                        subtitle = when (connectionState) {
                            is BleConnectionState.Connected -> connectionState.deviceName
                            else -> "BLE Wristband"
                        },
                        highlightColor = if (isConnected) VeilEmerald else MaterialTheme.colorScheme.tertiary
                    )

                    // System status item
                    TelemetryItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Security,
                        label = "System",
                        value = if (isAlertActive) "ALERT" else "Normal",
                        subtitle = if (isAlertActive) "Requires Attention" else "Standby Mode",
                        highlightColor = if (isAlertActive) VeilEmergencyRed else VeilEmerald
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Battery % & Last Alert Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Battery item
                    TelemetryItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.BatteryChargingFull,
                        label = "Battery",
                        value = if (batteryPercent != null) "$batteryPercent%" else "–",
                        subtitle = if (batteryPercent != null) "GATT 0x180F" else "Service unavailable",
                        highlightColor = if (batteryPercent != null && batteryPercent > 20) VeilEmerald else MaterialTheme.colorScheme.tertiary
                    )

                    // Last Alert Time item
                    val formattedLastAlert = if (lastAlertTime != null) {
                        SimpleDateFormat("HH:mm, MMM d", Locale.getDefault()).format(Date(lastAlertTime))
                    } else {
                        "No alerts"
                    }
                    TelemetryItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        label = "Last Alert",
                        value = formattedLastAlert,
                        subtitle = if (lastAlertTime != null) "Logged in history" else "Clean record",
                        highlightColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SOS Action Button (Enabled & armed ONLY after wristband is connected)
        com.example.ui.components.SosActionButton(
            isConnected = isConnected,
            onActivateSos = onActivateSos,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Discovered Nearby Wristbands Section (Displayed inline if devices found or currently scanning)
        AnimatedVisibility(visible = discoveredDevices.isNotEmpty() || isScanning) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("discovered_wristbands_card"),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = VeilEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NEARBY WRISTBANDS (${discoveredDevices.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        if (isScanning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = VeilEmerald,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scanning...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VeilEmerald
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = onStartScan,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Rescan", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (discoveredDevices.isEmpty()) {
                        Text(
                            text = "Scanning for nearby BLE emergency wristbands...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            discoveredDevices.take(4).forEach { dev ->
                                DiscoveredDeviceMiniRow(
                                    device = dev,
                                    isConnected = when (connectionState) {
                                        is BleConnectionState.Connected -> connectionState.address.equals(dev.address, ignoreCase = true)
                                        else -> false
                                    },
                                    onConnect = { onConnectDiscoveredDevice(dev) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Scan for Nearby Wristbands / Device Discovery Sheet Trigger
            Button(
                onClick = {
                    showScannerSheet = true
                    onStartScan()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("open_ble_scanner_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilEmeraldDark,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothSearching,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isConnected) "Scan Other Wristbands" else "Scan & Discover Wristbands",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Quick Connect / Disconnect Wristband Button
            if (isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("disconnect_wristband_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Disconnect Wristband",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onConnectWristband,
                    enabled = !isScanning && !isConnecting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auto_connect_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Quick Connect Nearest VEIL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Send Test Alert Button (runs the exact same flow as real BLE alert)
            Button(
                onClick = onSendTestAlert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("send_test_alert_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilEmergencyRed,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CrisisAlert,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Send Test Alert",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Quick Hardware Simulated Button Press helper (if connected)
            if (isConnected) {
                OutlinedButton(
                    onClick = onSimulateWristbandPress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulate_wristband_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VeilCyanAccent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VeilCyanAccent.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trigger Hardware Button (BLE Notify)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DiscoveredDeviceMiniRow(
    device: DiscoveredBleDevice,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (device.isVeilDevice) VeilEmerald.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (device.isVeilDevice) VeilEmerald.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (device.isVeilDevice) Icons.Default.Shield else Icons.Default.Watch,
                        contentDescription = null,
                        tint = if (device.isVeilDevice) VeilEmerald else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (device.isVeilDevice) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• VEIL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = VeilEmerald
                            )
                        }
                    }
                    Text(
                        text = "${device.address}  •  ${device.rssi} dBm",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (isConnected) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VeilEmerald.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = VeilEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = VeilEmerald
                        )
                    }
                }
            } else {
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (device.isVeilDevice) VeilEmeraldDark else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (device.isVeilDevice) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Connect", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subtitle: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = highlightColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = highlightColor,
                maxLines = 1
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.tertiary,
                maxLines = 1
            )
        }
    }
}
