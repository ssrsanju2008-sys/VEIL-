package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.VeilCyanAccent
import com.example.ui.theme.VeilEmerald
import com.example.ui.theme.VeilEmergencyRed

@Composable
fun TopSafetyBar(
    isSimulated: Boolean,
    batteryPercent: Int?,
    isConnected: Boolean,
    isAlertActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isAlertActive -> VeilEmergencyRed.copy(alpha = 0.2f)
                            isConnected -> VeilEmerald.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isAlertActive -> VeilEmergencyRed
                            isConnected -> VeilEmerald.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.outline
                        },
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "VEIL Shield",
                    tint = when {
                        isAlertActive -> VeilEmergencyRed
                        isConnected -> VeilEmerald
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "VEIL",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Emergency Link System",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSimulated) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(VeilCyanAccent.copy(alpha = 0.15f))
                        .border(1.dp, VeilCyanAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("simulated_bluetooth_badge")
                ) {
                    Text(
                        text = "SIMULATED BLE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = VeilCyanAccent
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (batteryPercent != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (batteryPercent > 20) VeilEmerald else VeilEmergencyRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$batteryPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
