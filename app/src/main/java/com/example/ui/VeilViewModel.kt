package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble.BleConnectionState
import com.example.ble.BleManager
import com.example.ble.HeartRatePoint
import com.example.data.UserPreferences
import com.example.data.VeilDatabase
import com.example.data.model.AlertEntity
import com.example.data.model.EmergencyContactEntity
import com.example.emergency.ActiveAlertState
import com.example.emergency.EmergencyAlertController
import com.example.emergency.EmergencyHapticsService
import com.example.emergency.EmergencyLocationService
import com.example.emergency.EmergencySirenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VeilViewModel(application: Application) : AndroidViewModel(application) {

    val database = VeilDatabase.getDatabase(application)
    val preferences = UserPreferences(application)

    private val locationService = EmergencyLocationService(application)
    private val sirenService = EmergencySirenService(viewModelScope)
    private val hapticsService = EmergencyHapticsService(application)
    private val notificationService = com.example.emergency.EmergencyNotificationService(application)

    val bleManager = BleManager(application, viewModelScope, preferences)
    val alertController = EmergencyAlertController(
        context = application,
        scope = viewModelScope,
        database = database,
        locationService = locationService,
        sirenService = sirenService,
        hapticsService = hapticsService,
        notificationService = notificationService
    )

    // BLE States
    val connectionState: StateFlow<BleConnectionState> = bleManager.connectionState
    val batteryPercent: StateFlow<Int?> = bleManager.batteryPercent
    val heartRateBpm: StateFlow<Int?> = bleManager.heartRateBpm
    val heartRateHistory: StateFlow<List<HeartRatePoint>> = bleManager.heartRateHistory
    val isSimulated: StateFlow<Boolean> = bleManager.isSimulated
    val discoveredDevices: StateFlow<List<com.example.ble.DiscoveredBleDevice>> = bleManager.discoveredDevices
    val isScanning: StateFlow<Boolean> = bleManager.isScanning

    // Alert States
    val activeAlert: StateFlow<ActiveAlertState?> = alertController.activeAlert
    val lastAlertTime: StateFlow<Long?> = alertController.lastAlertTime

    // Database Flows
    val alertsHistory: StateFlow<List<AlertEntity>> = database.alertDao()
        .getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = database.emergencyContactDao()
        .getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User settings
    private val _userName = MutableStateFlow(preferences.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        // Observe BLE alert notifications to trigger unified emergency flow
        viewModelScope.launch {
            bleManager.alertTriggerEvents.collect { source ->
                alertController.triggerAlert(source)
            }
        }

        // Initialize sample emergency contact if database is empty on first run
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = database.emergencyContactDao().getAllContactsList()
            if (contacts.isEmpty()) {
                database.emergencyContactDao().insertContact(
                    EmergencyContactEntity(
                        name = "Sarah Connor",
                        phoneNumber = "+14155552671",
                        relationship = "Emergency Contact / Family"
                    )
                )
            }
        }
    }

    fun scanAndConnect() {
        bleManager.scanAndConnect()
    }

    fun startScanning() {
        bleManager.startScanning()
    }

    fun stopScanning() {
        bleManager.stopScanning()
    }

    fun connectToDiscoveredDevice(device: com.example.ble.DiscoveredBleDevice) {
        bleManager.connectToDevice(device)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun triggerTestAlert() {
        alertController.triggerAlert("Test alert")
    }

    fun triggerSosAlert() {
        if (connectionState.value.isConnected) {
            alertController.triggerAlert("SOS Action Button")
        }
    }

    fun triggerWristbandSimulatedAlert() {
        bleManager.simulateButtonPress()
    }

    fun dismissAlert() {
        alertController.dismissAlert()
    }

    fun updateUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            preferences.userName = trimmed
            _userName.value = trimmed
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        bleManager.setSimulationMode(enabled)
    }

    fun toggleSimulatedHeartRate(enabled: Boolean) {
        bleManager.toggleSimulatedHeartRate(enabled)
    }

    fun addContact(name: String, phone: String, relationship: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.emergencyContactDao().insertContact(
                EmergencyContactEntity(
                    name = name.trim(),
                    phoneNumber = phone.trim(),
                    relationship = relationship.trim().ifEmpty { "Emergency Contact" }
                )
            )
        }
    }

    fun updateContact(contact: EmergencyContactEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.emergencyContactDao().updateContact(contact)
        }
    }

    fun deleteContact(contact: EmergencyContactEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.emergencyContactDao().deleteContact(contact)
        }
    }

    fun clearAlertHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            database.alertDao().clearAllAlerts()
        }
    }

    fun generateEmergencySmsText(location: com.example.emergency.EmergencyLocation?): String {
        val user = _userName.value
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val locStr = if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "Location retrieving / unavailable"
        }
        return "EMERGENCY! $user needs help. VEIL alert at $timeStr. Location: $locStr"
    }

    fun sendSmsToContacts(context: Context, location: com.example.emergency.EmergencyLocation?) {
        val contacts = emergencyContacts.value
        val message = generateEmergencySmsText(location)
        if (contacts.isEmpty()) {
            // Open SMS composer without recipient
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        val phoneNumbers = contacts.joinToString(";") { it.phoneNumber }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumbers")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general send intent
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$phoneNumbers")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun callContact(context: Context, phoneNumber: String) {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun sendWhatsApp(context: Context, phoneNumber: String, location: com.example.emergency.EmergencyLocation?) {
        val message = Uri.encode(generateEmergencySmsText(location))
        val cleanNumber = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
        val url = "https://wa.me/$cleanNumber?text=$message"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser or standard share
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        alertController.dismissAlert()
        bleManager.disconnect()
    }
}
