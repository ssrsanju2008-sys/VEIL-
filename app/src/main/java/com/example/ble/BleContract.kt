package com.example.ble

import java.util.UUID

object BleContract {
    const val DEVICE_NAME_PREFIX = "VEIL"

    // VEIL Primary Service & Alert Characteristic
    val SERVICE_UUID: UUID = UUID.fromString("7e1a0001-5645-494c-0000-000000000001")
    val ALERT_CHAR_UUID: UUID = UUID.fromString("7e1a0002-5645-494c-0000-000000000001")

    // Standard BLE Battery Service & Level Characteristic
    val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    val BATTERY_LEVEL_CHAR_UUID: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

    // Standard BLE Heart Rate Service & Measurement Characteristic
    val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    // Standard Client Characteristic Configuration Descriptor (CCCD) for notifications
    val CCCD_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}

data class DiscoveredBleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isVeilDevice: Boolean = true
) {
    val signalStrength: String
        get() = when {
            rssi >= -60 -> "Strong"
            rssi >= -75 -> "Good"
            rssi >= -85 -> "Fair"
            else -> "Weak"
        }

    val signalBars: Int
        get() = when {
            rssi >= -60 -> 4
            rssi >= -72 -> 3
            rssi >= -84 -> 2
            else -> 1
        }
}

sealed class BleConnectionState {
    object Disconnected : BleConnectionState()
    object Scanning : BleConnectionState()
    data class Connecting(val deviceName: String) : BleConnectionState()
    data class Connected(val deviceName: String, val address: String) : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()

    val isConnected: Boolean
        get() = this is Connected
}

data class HeartRatePoint(
    val timestamp: Long,
    val bpm: Int
)
