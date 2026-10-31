package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ble.BleContract
import com.example.data.model.EmergencyContactEntity
import com.example.ui.theme.VeilCyanAccent
import com.example.ui.theme.VeilEmerald
import com.example.ui.theme.VeilEmeraldDark
import com.example.ui.theme.VeilEmergencyRed

@Composable
fun SettingsScreen(
    userName: String,
    emergencyContacts: List<EmergencyContactEntity>,
    isSimulated: Boolean,
    onUpdateUserName: (String) -> Unit,
    onAddContact: (String, String, String) -> Unit,
    onUpdateContact: (EmergencyContactEntity) -> Unit,
    onDeleteContact: (EmergencyContactEntity) -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<EmergencyContactEntity?>(null) }

    // Check system permission statuses
    val hasBleScan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    } else true

    val hasBleConnect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else true

    val hasLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasSms = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    // Dialog for Editing User Name
    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit User Name") },
            text = {
                Column {
                    Text(
                        text = "This name appears in emergency alert SMS messages sent to your contacts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("user_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUserName(tempName)
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for Adding / Editing Emergency Contact
    if (showAddContactDialog || contactToEdit != null) {
        val editing = contactToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var phone by remember { mutableStateOf(editing?.phoneNumber ?: "") }
        var relationship by remember { mutableStateOf(editing?.relationship ?: "Family / Responder") }

        AlertDialog(
            onDismissRequest = {
                showAddContactDialog = false
                contactToEdit = null
            },
            title = { Text(if (editing != null) "Edit Contact" else "Add Emergency Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contact Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("contact_name_input")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (+Country Code)") },
                        placeholder = { Text("+1 415 555 0199") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("contact_phone_input")
                    )
                    OutlinedTextField(
                        value = relationship,
                        onValueChange = { relationship = it },
                        label = { Text("Relationship / Role") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("contact_role_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && phone.isNotBlank(),
                    onClick = {
                        if (editing != null) {
                            onUpdateContact(
                                editing.copy(
                                    name = name.trim(),
                                    phoneNumber = phone.trim(),
                                    relationship = relationship.trim()
                                )
                            )
                        } else {
                            onAddContact(name.trim(), phone.trim(), relationship.trim())
                        }
                        showAddContactDialog = false
                        contactToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddContactDialog = false
                        contactToEdit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section 1: User Profile Section
        item {
            Text(
                text = "USER PROFILE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = VeilEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Included in emergency distress SMS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEditNameDialog = true },
                        modifier = Modifier.testTag("edit_user_name_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit name",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Section 2: Emergency Contacts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EMERGENCY CONTACTS (${emergencyContacts.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )

                TextButton(
                    onClick = { showAddContactDialog = true },
                    modifier = Modifier.testTag("add_contact_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Contact")
                }
            }
        }

        items(emergencyContacts, key = { it.id }) { contact ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${contact.relationship} • ${contact.phoneNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Row {
                        IconButton(onClick = { contactToEdit = contact }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { onDeleteContact(contact) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Permissions Status Screen
        item {
            Text(
                text = "DEVICE PERMISSIONS STATUS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PermissionStatusRow(
                        title = "Bluetooth Scan & Connect",
                        description = "Required to discover and pair with ESP32 wristband",
                        isGranted = hasBleScan && hasBleConnect
                    )
                    PermissionStatusRow(
                        title = "GPS High-Accuracy Location",
                        description = "Coordinates sent to emergency contacts during alert",
                        isGranted = hasLocation
                    )
                    PermissionStatusRow(
                        title = "SMS & Phone Dispatch",
                        description = "Immediate 1-tap dispatch to saved emergency contacts",
                        isGranted = hasSms
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRequestPermissions,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Request Permissions", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Open App Settings", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Section 4: VEIL Device & BLE Specifications
        item {
            Text(
                text = "VEIL DEVICE & BLE CONTRACT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ESP32 Wristband Peripheral Specifications",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    UuidSpecItem(
                        label = "Scan Name Prefix",
                        value = "${BleContract.DEVICE_NAME_PREFIX}*",
                        onCopy = { clipboardManager.setText(AnnotatedString(BleContract.DEVICE_NAME_PREFIX)) }
                    )

                    UuidSpecItem(
                        label = "Emergency Service UUID",
                        value = BleContract.SERVICE_UUID.toString(),
                        onCopy = { clipboardManager.setText(AnnotatedString(BleContract.SERVICE_UUID.toString())) }
                    )

                    UuidSpecItem(
                        label = "Alert Characteristic (NOTIFY)",
                        value = BleContract.ALERT_CHAR_UUID.toString(),
                        onCopy = { clipboardManager.setText(AnnotatedString(BleContract.ALERT_CHAR_UUID.toString())) }
                    )

                    UuidSpecItem(
                        label = "Battery Service",
                        value = "GATT 0x180F (Char 0x2A19)",
                        onCopy = { clipboardManager.setText(AnnotatedString("0000180f-0000-1000-8000-00805f9b34fb")) }
                    )

                    UuidSpecItem(
                        label = "Heart Rate Service (Optional)",
                        value = "GATT 0x180D (Char 0x2A37)",
                        onCopy = { clipboardManager.setText(AnnotatedString("0000180d-0000-1000-8000-00805f9b34fb")) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onOpenScanner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("settings_scan_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VeilEmeraldDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan for Nearby Emergency Wristbands", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Section 5: Simulation Mode Toggle
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Simulated Bluetooth Mode",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isSimulated) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VeilCyanAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = VeilCyanAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Emulates ESP32 VEIL wristband hardware on devices/emulators without native Bluetooth LE.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Switch(
                        checked = isSimulated,
                        onCheckedChange = { onToggleSimulation(it) },
                        modifier = Modifier.testTag("simulation_mode_switch")
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    description: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isGranted) VeilEmerald.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGranted) "GRANTED" else "REQUIRED",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isGranted) VeilEmerald else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun UuidSpecItem(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy UUID",
                    tint = VeilCyanAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
