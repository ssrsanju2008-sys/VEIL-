package com.example

import com.example.ble.BleConnectionState
import com.example.ble.BleContract
import com.example.ble.DiscoveredBleDevice
import com.example.emergency.EmergencyLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun bleContract_uuidsMatchEsp32Firmware() {
        assertEquals("VEIL", BleContract.DEVICE_NAME_PREFIX)
        assertEquals(
            UUID.fromString("7e1a0001-5645-494c-0000-000000000001"),
            BleContract.SERVICE_UUID
        )
        assertEquals(
            UUID.fromString("7e1a0002-5645-494c-0000-000000000001"),
            BleContract.ALERT_CHAR_UUID
        )
        assertEquals(
            UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"),
            BleContract.BATTERY_SERVICE_UUID
        )
        assertEquals(
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"),
            BleContract.HEART_RATE_SERVICE_UUID
        )
    }

    @Test
    fun sosButton_connectionGateVerification() {
        val disconnectedState = BleConnectionState.Disconnected
        assertFalse("SOS button must remain locked when disconnected", disconnectedState.isConnected)

        val scanningState = BleConnectionState.Scanning
        assertFalse("SOS button must remain locked while scanning", scanningState.isConnected)

        val connectingState = BleConnectionState.Connecting("VEIL-Wristband-E8")
        assertFalse("SOS button must remain locked while connecting", connectingState.isConnected)

        val connectedState = BleConnectionState.Connected("VEIL-Wristband-E8", "AA:BB:CC:11:22:33")
        assertTrue("SOS button must be unlocked and armed when connected", connectedState.isConnected)
    }

    @Test
    fun discoveredBleDevice_signalStrengthAndBars() {
        val strongDevice = DiscoveredBleDevice("VEIL-Wristband-E8", "AA:BB:CC:DD:EE:01", -55, isVeilDevice = true)
        val goodDevice = DiscoveredBleDevice("VEIL-Band-A4", "AA:BB:CC:DD:EE:02", -70, isVeilDevice = true)
        val fairDevice = DiscoveredBleDevice("VEIL-SmartGuard", "AA:BB:CC:DD:EE:03", -80, isVeilDevice = true)
        val weakDevice = DiscoveredBleDevice("Generic-Beacon", "AA:BB:CC:DD:EE:04", -92, isVeilDevice = false)

        assertEquals("Strong", strongDevice.signalStrength)
        assertEquals(4, strongDevice.signalBars)

        assertEquals("Good", goodDevice.signalStrength)
        assertEquals(3, goodDevice.signalBars)

        assertEquals("Fair", fairDevice.signalStrength)
        assertEquals(2, fairDevice.signalBars)

        assertEquals("Weak", weakDevice.signalStrength)
        assertEquals(1, weakDevice.signalBars)
    }

    @Test
    fun emergencyLocation_mapsUrlAndFormatting() {
        val loc = EmergencyLocation(
            latitude = 37.77492,
            longitude = -122.41941,
            accuracyMeters = 8.5f
        )
        assertEquals("https://maps.google.com/?q=37.77492,-122.41941", loc.mapsUrl)
        assertTrue(loc.formattedCoordinates().contains("37.77492° N"))
        assertTrue(loc.formattedCoordinates().contains("122.41941° W"))
    }
}
