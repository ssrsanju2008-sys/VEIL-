package com.example.ble

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MockBleService(
    private val scope: CoroutineScope
) : VeilBleService {

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _batteryPercent = MutableStateFlow<Int?>(null)
    override val batteryPercent: StateFlow<Int?> = _batteryPercent.asStateFlow()

    private val _heartRateBpm = MutableStateFlow<Int?>(null)
    override val heartRateBpm: StateFlow<Int?> = _heartRateBpm.asStateFlow()

    private val _heartRateHistory = MutableStateFlow<List<HeartRatePoint>>(emptyList())
    override val heartRateHistory: StateFlow<List<HeartRatePoint>> = _heartRateHistory.asStateFlow()

    private val _alertTriggerEvents = MutableSharedFlow<String>(extraBufferCapacity = 5)
    override val alertTriggerEvents: SharedFlow<String> = _alertTriggerEvents.asSharedFlow()

    private val _isSimulated = MutableStateFlow(true)
    override val isSimulated: StateFlow<Boolean> = _isSimulated.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBleDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<DiscoveredBleDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var scanJob: Job? = null
    private var connectJob: Job? = null

    override fun startScanning() {
        scanJob?.cancel()
        _isScanning.value = true
        _connectionState.value = BleConnectionState.Scanning

        scanJob = scope.launch(Dispatchers.Default) {
            val list = mutableListOf<DiscoveredBleDevice>()
            _discoveredDevices.value = emptyList()

            // Discovered device 1
            delay(400)
            list.add(
                DiscoveredBleDevice(
                    name = "VEIL-Wristband-E8",
                    address = "E8:6B:EA:21:40:11",
                    rssi = -56,
                    isVeilDevice = true
                )
            )
            _discoveredDevices.value = list.toList()

            // Discovered device 2
            delay(600)
            list.add(
                DiscoveredBleDevice(
                    name = "VEIL-Band-A4",
                    address = "A4:C1:38:92:14:02",
                    rssi = -71,
                    isVeilDevice = true
                )
            )
            _discoveredDevices.value = list.toList()

            // Discovered device 3
            delay(800)
            list.add(
                DiscoveredBleDevice(
                    name = "VEIL-SmartGuard-Pro",
                    address = "C0:49:EF:18:22:90",
                    rssi = -83,
                    isVeilDevice = true
                )
            )
            _discoveredDevices.value = list.toList()

            // Discovered generic BLE peripheral nearby
            delay(700)
            list.add(
                DiscoveredBleDevice(
                    name = "Generic-BLE-Beacon",
                    address = "F1:22:33:44:55:66",
                    rssi = -92,
                    isVeilDevice = false
                )
            )
            _discoveredDevices.value = list.toList()
        }
    }

    override fun stopScanning() {
        scanJob?.cancel()
        _isScanning.value = false
        if (_connectionState.value is BleConnectionState.Scanning) {
            _connectionState.value = BleConnectionState.Disconnected
        }
    }

    override fun connectToDevice(device: DiscoveredBleDevice) {
        stopScanning()
        connectJob?.cancel()
        connectJob = scope.launch(Dispatchers.Default) {
            _connectionState.value = BleConnectionState.Connecting(device.name)
            delay(800)
            _connectionState.value = BleConnectionState.Connected(
                deviceName = device.name,
                address = device.address
            )
            _batteryPercent.value = 92
        }
    }

    override fun scanAndConnect() {
        startScanning()
        // Auto-connect to first VEIL device after a short simulated discovery
        scope.launch(Dispatchers.Default) {
            delay(1200)
            val veilDevice = _discoveredDevices.value.firstOrNull { it.isVeilDevice }
                ?: DiscoveredBleDevice("VEIL-Wristband-E8", "E8:6B:EA:21:40:11", -58, isVeilDevice = true)
            connectToDevice(veilDevice)
        }
    }

    override fun disconnect() {
        connectJob?.cancel()
        stopScanning()
        _connectionState.value = BleConnectionState.Disconnected
        _batteryPercent.value = null
        _heartRateBpm.value = null
    }

    override fun simulateButtonPress() {
        scope.launch {
            _alertTriggerEvents.emit("Wristband button")
        }
    }

    override fun setSimulationMode(enabled: Boolean) {
        _isSimulated.value = enabled
    }

    fun toggleSimulatedHeartRate(enabled: Boolean) {
        if (enabled && _connectionState.value.isConnected) {
            val baseBpm = 74
            _heartRateBpm.value = baseBpm
            val initialList = mutableListOf<HeartRatePoint>()
            val now = System.currentTimeMillis()
            for (i in 10 downTo 0) {
                initialList.add(
                    HeartRatePoint(
                        timestamp = now - (i * 2000),
                        bpm = baseBpm + ((-3..4).random())
                    )
                )
            }
            _heartRateHistory.value = initialList
        } else {
            _heartRateBpm.value = null
            _heartRateHistory.value = emptyList()
        }
    }
}
