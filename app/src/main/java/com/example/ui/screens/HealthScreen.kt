package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.HeartRatePoint
import com.example.ui.components.HeartRateTrendChart
import com.example.ui.theme.VeilCyanAccent
import com.example.ui.theme.VeilEmerald
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HealthScreen(
    heartRateBpm: Int?,
    heartRateHistory: List<HeartRatePoint>,
    isConnected: Boolean,
    isSimulated: Boolean,
    onToggleSimulatedHr: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "BIOMETRIC TELEMETRY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        if (heartRateBpm == null || !isConnected) {
            // STRICT EMPTY STATE WHEN NO HEART-RATE SENSOR IS CONNECTED
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("hr_empty_state_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No heart-rate sensor connected",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (!isConnected) {
                                "Connect your VEIL wristband on the Home tab to detect GATT health telemetry."
                            } else {
                                "The connected VEIL wristband peripheral does not expose the standard BLE Heart Rate Service (0x180D / characteristic 0x2A37). Biometric telemetry is only displayed when genuine hardware is streaming."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scientific Integrity Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = VeilCyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VEIL strictly refuses to fabricate or fake biometric data.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        // Simulation mode testing toggle
                        if (isSimulated && isConnected) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Attach Simulated Sensor (0x180D)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Simulate ESP32 with MAX30102 optical sensor",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Switch(
                                    checked = false,
                                    onCheckedChange = { onToggleSimulatedHr(it) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ACTIVE REAL-TIME HEART RATE TELEMETRY
            val bpms = heartRateHistory.map { it.bpm }
            val minBpm = bpms.minOrNull() ?: heartRateBpm
            val maxBpm = bpms.maxOrNull() ?: heartRateBpm
            val avgBpm = if (bpms.isNotEmpty()) bpms.average().toInt() else heartRateBpm

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Heart Rate",
                                    tint = VeilCyanAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE PULSE",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = VeilCyanAccent
                                )
                            }

                            Text(
                                text = "GATT 0x180D ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VeilEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$heartRateBpm",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 48.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BPM",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Trend Line Chart
                        Text(
                            text = "TREND TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        HeartRateTrendChart(points = heartRateHistory)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Min / Avg / Max Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BpmStatBox(
                                label = "MIN",
                                value = "$minBpm",
                                modifier = Modifier.weight(1f)
                            )
                            BpmStatBox(
                                label = "AVG",
                                value = "$avgBpm",
                                modifier = Modifier.weight(1f)
                            )
                            BpmStatBox(
                                label = "MAX",
                                value = "$maxBpm",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "RECENT READINGS (${heartRateHistory.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            items(heartRateHistory.reversed().take(12)) { point ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(point.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${point.bpm} BPM",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = VeilCyanAccent
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BpmStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
