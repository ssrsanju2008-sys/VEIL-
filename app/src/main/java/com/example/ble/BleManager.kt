package com.example.ble

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.example.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BleManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val userPreferences: UserPreferences
) : VeilBleService {

    private val realService = RealBleService(context, scope)
    private val mockService = MockBleService(scope)

    private val isBleSupportedOnDevice: Boolean =
        BluetoothAdapter.getDefaultAdapter() != null

    private val _isSimulated = MutableStateFlow(
        !isBleSupportedOnDevice || userPreferences.isSimulatedBle
    )
    override val isSimulated: StateFlow<Boolean> = _isSimulated.asStateFlow()

    private val activeService: VeilBleService
        get() = if (_isSimulated.value) mockService else realService

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _batteryPercent = MutableStateFlow<Int?>(null)
    override val batteryPercent: StateFlow<Int?> = _batteryPercent.asStateFlow()

    private val _heartRateBpm = MutableStateFlow<Int?>(null)
    override val heartRateBpm: StateFlow<Int?> = _heartRateBpm.asStateFlow()

    private val _heartRateHistory = MutableStateFlow<List<HeartRatePoint>>(emptyList())
    override val heartRateHistory: StateFlow<List<HeartRatePoint>> = _heartRateHistory.asStateFlow()

    private val _alertTriggerEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    override val alertTriggerEvents: SharedFlow<String> = _alertTriggerEvents.asSharedFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBleDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<DiscoveredBleDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        scope.launch {
            _isSimulated.collect { simulated ->
                if (simulated) {
                    mockService.setSimulationMode(true)
                }
            }
        }

        scope.launch {
            realService.connectionState.collect {
                if (!_isSimulated.value) _connectionState.value = it
            }
        }
        scope.launch {
            mockService.connectionState.collect {
                if (_isSimulated.value) _connectionState.value = it
            }
        }

        scope.launch {
            realService.batteryPercent.collect {
                if (!_isSimulated.value) _batteryPercent.value = it
            }
        }
        scope.launch {
            mockService.batteryPercent.collect {
                if (_isSimulated.value) _batteryPercent.value = it
            }
        }

        scope.launch {
            realService.heartRateBpm.collect {
                if (!_isSimulated.value) _heartRateBpm.value = it
            }
        }
        scope.launch {
            mockService.heartRateBpm.collect {
                if (_isSimulated.value) _heartRateBpm.value = it
            }
        }

        scope.launch {
            realService.heartRateHistory.collect {
                if (!_isSimulated.value) _heartRateHistory.value = it
            }
        }
        scope.launch {
            mockService.heartRateHistory.collect {
                if (_isSimulated.value) _heartRateHistory.value = it
            }
        }

        scope.launch {
            realService.alertTriggerEvents.collect {
                _alertTriggerEvents.emit(it)
            }
        }
        scope.launch {
            mockService.alertTriggerEvents.collect {
                _alertTriggerEvents.emit(it)
            }
        }

        // Bridge discovered devices and scanning state
        scope.launch {
            realService.discoveredDevices.collect {
                if (!_isSimulated.value) _discoveredDevices.value = it
            }
        }
        scope.launch {
            mockService.discoveredDevices.collect {
                if (_isSimulated.value) _discoveredDevices.value = it
            }
        }

        scope.launch {
            realService.isScanning.collect {
                if (!_isSimulated.value) _isScanning.value = it
            }
        }
        scope.launch {
            mockService.isScanning.collect {
                if (_isSimulated.value) _isScanning.value = it
            }
        }
    }

    override fun startScanning() {
        activeService.startScanning()
    }

    override fun stopScanning() {
        activeService.stopScanning()
    }

    override fun connectToDevice(device: DiscoveredBleDevice) {
        activeService.connectToDevice(device)
    }

    override fun scanAndConnect() {
        activeService.scanAndConnect()
    }

    override fun disconnect() {
        activeService.disconnect()
    }

    override fun simulateButtonPress() {
        activeService.simulateButtonPress()
    }

    fun toggleSimulatedHeartRate(enabled: Boolean) {
        mockService.toggleSimulatedHeartRate(enabled)
    }

    override fun setSimulationMode(enabled: Boolean) {
        if (!isBleSupportedOnDevice && !enabled) {
            return
        }
        disconnect()
        userPreferences.isSimulatedBle = enabled
        _isSimulated.value = enabled
    }
}
