package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyContactEntity
import com.example.emergency.ActiveAlertState
import com.example.ui.theme.VeilEmergencyBright
import com.example.ui.theme.VeilEmergencyContainer
import com.example.ui.theme.VeilEmergencyDark
import com.example.ui.theme.VeilEmergencyRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmergencyTakeoverScreen(
    alertState: ActiveAlertState?,
    emergencyContacts: List<EmergencyContactEntity>,
    userName: String,
    onDismissAlert: () -> Unit,
    onSendSmsAll: () -> Unit,
    onCallContact: (String) -> Unit,
    onWhatsAppContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Urgent pulsing background animation
    val infiniteTransition = rememberInfiniteTransition(label = "emergencyPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val timestamp = alertState?.timestamp ?: System.currentTimeMillis()
    val formattedTime = SimpleDateFormat("HH:mm:ss, MMM d yyyy", Locale.getDefault()).format(Date(timestamp))
    val source = alertState?.source ?: "Manual Trigger"
    val location = alertState?.location

    val backgroundColor = if (alertState != null) {
        Color(0xFF2A0606)
    } else {
        MaterialTheme.colorScheme.background
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("emergency_takeover_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Siren / Emergency Beacon Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(VeilEmergencyRed.copy(alpha = 0.2f * pulseAlpha))
                    .border(2.dp, VeilEmergencyRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CrisisAlert,
                    contentDescription = "Alert Beacon",
                    tint = VeilEmergencyRed,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ALERT DETECTED",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = VeilEmergencyBright,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "SIREN & HAPTIC BROADCAST ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata Card (Source & Time)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF3F0B0B),
                border = androidx.compose.foundation.BorderStroke(1.dp, VeilEmergencyRed.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TRIGGER SOURCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFCA5A5)
                        )
                        Text(
                            text = source,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TIMESTAMP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFCA5A5)
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GPS Location Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E0E12),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B1D2F))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VeilEmergencyRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CURRENT GPS LOCATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFFFCA5A5)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (alertState?.isLocating == true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = VeilEmergencyBright,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Acquiring high-accuracy GPS fix (15s timeout)...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    } else if (location != null) {
                        Text(
                            text = location.formattedCoordinates(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Lat: ${location.latitude}, Lng: ${location.longitude}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Open in Maps Button
                        OutlinedButton(
                            onClick = {
                                val mapIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(VEIL+Emergency+Alert)")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(location.mapsUrl)
                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    context.startActivity(browserIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("open_in_maps_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open in Maps",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Text(
                            text = alertState?.locationError ?: "Location unavailable (GPS denied or timed out)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Action: Send SMS to All Contacts
            Button(
                onClick = onSendSmsAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("send_sms_all_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = VeilEmergencyDark
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Send SMS to All Contacts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Emergency Contacts Quick Action List
            Text(
                text = "EMERGENCY CONTACTS (${emergencyContacts.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (emergencyContacts.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E0E12)
                ) {
                    Text(
                        text = "No emergency contacts configured. Add contacts in Settings to send 1-tap alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFCA5A5),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                emergencyContacts.forEach { contact ->
                    EmergencyContactRow(
                        contact = contact,
                        onCall = { onCallContact(contact.phoneNumber) },
                        onWhatsApp = { onWhatsAppContact(contact.phoneNumber) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "I'm safe, dismiss alert" Button
            Button(
                onClick = onDismissAlert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("dismiss_alert_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "I'm safe, dismiss alert",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EmergencyContactRow(
    contact: EmergencyContactEntity,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF330C12),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7F1D1D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${contact.relationship} • ${contact.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFFCA5A5)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Call Button
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                        .testTag("call_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call ${contact.name}",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // WhatsApp Button
                IconButton(
                    onClick = onWhatsApp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366).copy(alpha = 0.2f))
                        .testTag("whatsapp_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "WhatsApp ${contact.name}",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
