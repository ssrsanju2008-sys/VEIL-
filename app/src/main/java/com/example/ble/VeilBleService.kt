package com.example.ble

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface VeilBleService {
    val connectionState: StateFlow<BleConnectionState>
    val batteryPercent: StateFlow<Int?>
    val heartRateBpm: StateFlow<Int?>
    val heartRateHistory: StateFlow<List<HeartRatePoint>>
    val alertTriggerEvents: SharedFlow<String>
    val isSimulated: StateFlow<Boolean>

    val discoveredDevices: StateFlow<List<DiscoveredBleDevice>>
    val isScanning: StateFlow<Boolean>

    fun startScanning()
    fun stopScanning()
    fun connectToDevice(device: DiscoveredBleDevice)
    fun scanAndConnect()
    fun disconnect()
    fun simulateButtonPress()
    fun setSimulationMode(enabled: Boolean)
}
