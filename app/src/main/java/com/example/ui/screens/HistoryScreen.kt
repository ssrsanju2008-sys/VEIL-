package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertEntity
import com.example.ui.theme.VeilCyanAccent
import com.example.ui.theme.VeilEmergencyRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    alerts: List<AlertEntity>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Alert History?") },
            text = { Text("This will permanently remove all stored emergency alert logs from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearHistory()
                    }
                ) {
                    Text("Clear All", color = VeilEmergencyRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INCIDENT HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "${alerts.size} logged events",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (alerts.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Clear history", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No emergency alerts recorded",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Alerts triggered via wristband or test button will appear here with location records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertHistoryCard(
                        alert = alert,
                        onOpenMap = { lat, lng ->
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:$lat,$lng?q=$lat,$lng(VEIL+Alert)")
                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://maps.google.com/?q=$lat,$lng")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                context.startActivity(browserIntent)
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryCard(
    alert: AlertEntity,
    onOpenMap: (Double, Double) -> Unit
) {
    val formattedTime = SimpleDateFormat("MMM d, yyyy • HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp))
    val hasLocation = alert.latitude != null && alert.longitude != null

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_history_card_${alert.id}")
            .then(
                if (hasLocation) {
                    Modifier.clickable { onOpenMap(alert.latitude!!, alert.longitude!!) }
                } else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(VeilEmergencyRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.source,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (hasLocation) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = VeilCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "%.4f, %.4f (±%.0fm)".format(
                                    alert.latitude,
                                    alert.longitude,
                                    alert.accuracyMeters ?: 20f
                                ),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Maps",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VeilCyanAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open Maps",
                                tint = VeilCyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No GPS fix acquired during this incident",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
