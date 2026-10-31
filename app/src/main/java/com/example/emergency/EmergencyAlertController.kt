package com.example.emergency

import android.content.Context
import android.util.Log
import com.example.data.VeilDatabase
import com.example.data.model.AlertEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "EmergencyAlertController"

data class ActiveAlertState(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String, // "Wristband button", "SOS Action Button", or "Test alert"
    val location: EmergencyLocation? = null,
    val isLocating: Boolean = false,
    val locationError: String? = null
)

class EmergencyAlertController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val database: VeilDatabase,
    private val locationService: EmergencyLocationService,
    private val sirenService: EmergencySirenService,
    private val hapticsService: EmergencyHapticsService,
    private val notificationService: EmergencyNotificationService
) {

    private val _activeAlert = MutableStateFlow<ActiveAlertState?>(null)
    val activeAlert: StateFlow<ActiveAlertState?> = _activeAlert.asStateFlow()

    private val _lastAlertTime = MutableStateFlow<Long?>(null)
    val lastAlertTime: StateFlow<Long?> = _lastAlertTime.asStateFlow()

    init {
        // Load latest alert from database on startup
        scope.launch {
            database.alertDao().getLatestAlert().collect { latest ->
                _lastAlertTime.value = latest?.timestamp
            }
        }
    }

    /**
     * Unified triggerAlert flow called by wristband BLE button, SOS action button, and Test Alert UI
     */
    fun triggerAlert(source: String) {
        Log.i(TAG, "Triggering emergency alert from source: $source")
        val timestamp = System.currentTimeMillis()
        _lastAlertTime.value = timestamp

        // 1. Immediately start Siren, Haptics, and System Alert Notification
        sirenService.startSiren()
        hapticsService.startVibration()
        notificationService.showEmergencyNotification(source, timestamp)

        // 2. Open full-screen emergency takeover immediately with locating state
        _activeAlert.value = ActiveAlertState(
            timestamp = timestamp,
            source = source,
            isLocating = true
        )

        // 3. Obtain high-accuracy location asynchronously with 15s timeout
        scope.launch(Dispatchers.IO) {
            val loc = locationService.getCurrentEmergencyLocation()

            _activeAlert.value = _activeAlert.value?.copy(
                location = loc,
                isLocating = false,
                locationError = if (loc == null) "Location unavailable or permission not granted" else null
            )

            // 4. Save to persistent History in Room Database
            try {
                val alertEntity = AlertEntity(
                    timestamp = timestamp,
                    source = source,
                    latitude = loc?.latitude,
                    longitude = loc?.longitude,
                    accuracyMeters = loc?.accuracyMeters,
                    isResolved = false
                )
                val id = database.alertDao().insertAlert(alertEntity)
                _activeAlert.value = _activeAlert.value?.copy(id = id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed saving alert to database", e)
            }
        }
    }

    /**
     * Dismiss the emergency alert, stop siren, vibration, and cancel notification
     */
    fun dismissAlert() {
        Log.i(TAG, "Dismissing active emergency alert")
        sirenService.stopSiren()
        hapticsService.stopVibration()
        notificationService.cancelEmergencyNotification()
        _activeAlert.value = null
    }
}
