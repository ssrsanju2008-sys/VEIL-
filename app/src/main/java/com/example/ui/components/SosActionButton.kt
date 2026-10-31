package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VeilEmergencyBright
import com.example.ui.theme.VeilEmergencyContainer
import com.example.ui.theme.VeilEmergencyDark
import com.example.ui.theme.VeilEmergencyRed

@Composable
fun SosActionButton(
    isConnected: Boolean,
    onActivateSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Pulsing transition for active SOS button
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulseTransition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosGlow"
    )

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CrisisAlert,
                    contentDescription = null,
                    tint = VeilEmergencyRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Activate Emergency SOS?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will immediately sound the high-decibel alert siren, start emergency haptics, post an urgent distress notification, and lock onto your GPS location for emergency contact dispatch.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onActivateSos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeilEmergencyRed),
                    modifier = Modifier.testTag("confirm_sos_button")
                ) {
                    Text("ACTIVATE NOW", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sos_action_card")
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                if (isConnected) {
                    showConfirmDialog = true
                } else {
                    Toast.makeText(
                        context,
                        "SOS is locked. Please connect your VEIL wristband first to enable distress link.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = if (isConnected) {
                Color(0xFF380808)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isConnected) 2.dp else 1.dp,
                    color = if (isConnected) {
                        VeilEmergencyRed.copy(alpha = pulseGlow)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Emblem Box
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) {
                                    VeilEmergencyRed.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(
                                1.5.dp,
                                if (isConnected) VeilEmergencyRed else MaterialTheme.colorScheme.outline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CrisisAlert else Icons.Default.Lock,
                            contentDescription = if (isConnected) "SOS Active" else "SOS Locked",
                            tint = if (isConnected) VeilEmergencyBright else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EMERGENCY SOS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = if (isConnected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isConnected) VeilEmergencyRed.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isConnected) "ARMED" else "LOCKED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = if (isConnected) VeilEmergencyBright else MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isConnected) {
                                "Tap to sound high-decibel siren & notify contacts"
                            } else {
                                "Requires active VEIL wristband connection"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = if (isConnected) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Action Indicator Button
                Button(
                    onClick = {
                        if (isConnected) {
                            showConfirmDialog = true
                        } else {
                            Toast.makeText(
                                context,
                                "Connect your VEIL wristband first to enable SOS.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) VeilEmergencyRed else MaterialTheme.colorScheme.surface,
                        contentColor = if (isConnected) Color.White else MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.testTag("sos_trigger_button")
                ) {
                    Text(
                        text = if (isConnected) "SOS" else "LOCK",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}
