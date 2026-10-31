package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.BleConnectionState
import com.example.ble.DiscoveredBleDevice
import com.example.ui.theme.VeilCyanAccent
import com.example.ui.theme.VeilEmerald
import com.example.ui.theme.VeilEmeraldDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleScannerSheet(
    isScanning: Boolean,
    discoveredDevices: List<DiscoveredBleDevice>,
    connectionState: BleConnectionState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (DiscoveredBleDevice) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filterVeilOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isScanning && !connectionState.isConnected) {
            onStartScan()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onStopScan()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("ble_scanner_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header with Title and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VeilEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            tint = VeilEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Discover Wristbands",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bluetooth Low Energy Scanner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                IconButton(
                    onClick = {
                        onStopScan()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("close_scanner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Scanner",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Radar Scanning Status Banner
            RadarScanningCard(
                isScanning = isScanning,
                deviceCount = discoveredDevices.size,
                onToggleScan = {
                    if (isScanning) onStopScan() else onStartScan()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter bar: All vs VEIL only
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEARBY WRISTBANDS (${discoveredDevices.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )

                FilterChip(
                    selected = filterVeilOnly,
                    onClick = { filterVeilOnly = !filterVeilOnly },
                    label = {
                        Text(
                            text = "VEIL Only",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (filterVeilOnly) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VeilEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = VeilEmerald
                    ),
                    modifier = Modifier.testTag("filter_veil_only_chip")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val displayDevices = if (filterVeilOnly) {
                discoveredDevices.filter { it.isVeilDevice }
            } else {
                discoveredDevices
            }

            // Discovered Devices List
            if (displayDevices.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = VeilEmerald,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Listening for BLE wristband advertisements...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ensure the ESP32 wristband is powered on and within range (approx. 10m).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No wristbands found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onStartScan,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VeilEmeraldDark)
                            ) {
                                Text("Scan Again")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayDevices, key = { it.address }) { device ->
                        DiscoveredDeviceRow(
                            device = device,
                            connectionState = connectionState,
                            onConnect = { onConnectDevice(device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RadarScanningCard(
    isScanning: Boolean,
    deviceCount: Int,
    onToggleScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarTransition")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isScanning) VeilEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        Canvas(modifier = Modifier.size(44.dp)) {
                            drawCircle(
                                color = VeilEmerald.copy(alpha = 0.2f),
                                radius = size.minDimension / 2f,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = VeilEmerald.copy(alpha = 0.4f),
                                radius = size.minDimension / 3.5f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(sweepAngle),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(VeilEmerald)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isScanning) "Active BLE Scanning" else "Scan Idle",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isScanning) VeilEmerald else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isScanning) "Discovered $deviceCount emergency peripherals" else "Tap Scan to search area",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Button(
                onClick = onToggleScan,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) MaterialTheme.colorScheme.surface else VeilEmeraldDark,
                    contentColor = if (isScanning) MaterialTheme.colorScheme.onSurface else Color.White
                ),
                border = if (isScanning) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                modifier = Modifier.testTag("toggle_scan_button")
            ) {
                if (isScanning) {
                    Text("Stop", style = MaterialTheme.typography.labelMedium)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceRow(
    device: DiscoveredBleDevice,
    connectionState: BleConnectionState,
    onConnect: () -> Unit
) {
    val isTargetConnected = when (connectionState) {
        is BleConnectionState.Connected -> connectionState.address.equals(device.address, ignoreCase = true)
        else -> false
    }

    val isConnecting = when (connectionState) {
        is BleConnectionState.Connecting -> connectionState.deviceName == device.name
        else -> false
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_row_${device.address.replace(':', '_')}"),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (device.isVeilDevice) VeilEmerald.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (device.isVeilDevice) VeilEmerald.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (device.isVeilDevice) Icons.Default.Shield else Icons.Default.Watch,
                        contentDescription = null,
                        tint = if (device.isVeilDevice) VeilEmerald else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (device.isVeilDevice) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VeilEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VEIL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = VeilEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Signal Quality indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SignalStrengthBars(bars = device.signalBars, isVeil = device.isVeilDevice)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${device.rssi} dBm • ${device.signalStrength}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            if (isTargetConnected) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VeilEmerald.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = VeilEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VeilEmerald
                        )
                    }
                }
            } else if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = VeilEmerald,
                    strokeWidth = 2.5.dp
                )
            } else {
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (device.isVeilDevice) VeilEmeraldDark else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (device.isVeilDevice) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("connect_btn_${device.address.replace(':', '_')}")
                ) {
                    Text("Connect", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SignalStrengthBars(bars: Int, isVeil: Boolean) {
    val barColor = if (isVeil) VeilEmerald else VeilCyanAccent
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(10.dp)
    ) {
        for (i in 1..4) {
            val active = i <= bars
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((i * 2.5).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (active) barColor else Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}
