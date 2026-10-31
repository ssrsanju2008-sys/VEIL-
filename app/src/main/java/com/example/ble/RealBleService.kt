package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
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

private const val TAG = "RealBleService"

class RealBleService(
    private val context: Context,
    private val scope: CoroutineScope
) : VeilBleService {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter

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

    private val _isSimulated = MutableStateFlow(false)
    override val isSimulated: StateFlow<Boolean> = _isSimulated.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBleDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<DiscoveredBleDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var activeGatt: BluetoothGatt? = null
    private var scanCallback: ScanCallback? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var autoReconnectDevice: BluetoothDevice? = null
    private var reconnectJob: Job? = null

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBlePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    override fun startScanning() {
        if (!hasBlePermissions()) {
            _connectionState.value = BleConnectionState.Error("Bluetooth and Location permissions are required to scan for BLE wristbands.")
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth is turned off. Please enable Bluetooth.")
            return
        }

        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            _connectionState.value = BleConnectionState.Error("Bluetooth LE scanner is unavailable on this device.")
            return
        }

        stopScanning()
        _discoveredDevices.value = emptyList()
        _isScanning.value = true
        _connectionState.value = BleConnectionState.Scanning

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let { handleScanResult(it) }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { handleScanResult(it) }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "BLE scan failed with error code: $errorCode")
                _isScanning.value = false
                _connectionState.value = BleConnectionState.Error("BLE scan failed (code $errorCode)")
            }
        }

        scanCallback = callback
        try {
            scanner.startScan(emptyList<ScanFilter>(), settings, callback)

            // Auto-stop scan after 20 seconds
            mainHandler.postDelayed({
                if (_isScanning.value) {
                    stopScanning()
                }
            }, 20000)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE scan", e)
            _isScanning.value = false
            _connectionState.value = BleConnectionState.Error("Could not start scan: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        scanCallback?.let {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping scan", e)
            }
        }
        scanCallback = null
        _isScanning.value = false
        if (_connectionState.value is BleConnectionState.Scanning) {
            _connectionState.value = BleConnectionState.Disconnected
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleScanResult(result: ScanResult) {
        val device = result.device
        val name = device.name ?: result.scanRecord?.deviceName ?: ""
        val address = device.address ?: return
        val rssi = result.rssi
        val isVeil = name.startsWith(BleContract.DEVICE_NAME_PREFIX, ignoreCase = true)

        val displayName = if (name.isNotBlank()) name else "Unnamed BLE Device"

        val currentList = _discoveredDevices.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.address == address }

        val newDevice = DiscoveredBleDevice(
            name = displayName,
            address = address,
            rssi = rssi,
            lastSeenTimestamp = System.currentTimeMillis(),
            isVeilDevice = isVeil
        )

        if (existingIndex >= 0) {
            currentList[existingIndex] = newDevice
        } else {
            currentList.add(newDevice)
        }

        // Sort: VEIL devices first, then descending signal strength (highest RSSI)
        currentList.sortWith(
            compareByDescending<DiscoveredBleDevice> { it.isVeilDevice }
                .thenByDescending { it.rssi }
        )

        _discoveredDevices.value = currentList
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: DiscoveredBleDevice) {
        stopScanning()
        disconnectGatt()

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth is turned off.")
            return
        }

        try {
            val remoteDevice = adapter.getRemoteDevice(device.address)
            connectToGatt(remoteDevice, device.name)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid Bluetooth address: ${device.address}", e)
            _connectionState.value = BleConnectionState.Error("Cannot connect to address: ${device.address}")
        }
    }

    @SuppressLint("MissingPermission")
    override fun scanAndConnect() {
        startScanning()

        // If a VEIL wristband is discovered during scan, connect automatically
        scope.launch {
            val startTime = System.currentTimeMillis()
            while (_isScanning.value && System.currentTimeMillis() - startTime < 15000) {
                val target = _discoveredDevices.value.firstOrNull { it.isVeilDevice }
                if (target != null) {
                    Log.d(TAG, "Auto-connecting to discovered VEIL device: ${target.name}")
                    connectToDevice(target)
                    break
                }
                delay(500)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToGatt(device: BluetoothDevice, preferredName: String) {
        _connectionState.value = BleConnectionState.Connecting(preferredName.ifEmpty { "VEIL Wristband" })
        autoReconnectDevice = device

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                val devName = preferredName.ifEmpty { gatt.device.name ?: "VEIL Wristband" }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT server on $devName")
                    _connectionState.value = BleConnectionState.Connected(devName, gatt.device.address)
                    reconnectJob?.cancel()
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from GATT server on $devName (status $status)")
                    _connectionState.value = BleConnectionState.Disconnected
                    _batteryPercent.value = null
                    _heartRateBpm.value = null
                    disconnectGatt()

                    scheduleAutoReconnect(device, devName)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    setupVeilServices(gatt)
                } else {
                    Log.w(TAG, "onServicesDiscovered error: $status")
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                handleCharacteristicData(characteristic.uuid.toString(), characteristic.value)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                handleCharacteristicData(characteristic.uuid.toString(), value)
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    handleCharacteristicData(characteristic.uuid.toString(), characteristic.value)
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    handleCharacteristicData(characteristic.uuid.toString(), value)
                }
            }
        }

        try {
            activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, gattCallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to device", e)
            _connectionState.value = BleConnectionState.Error("Connection error: ${e.message}")
        }
    }

    private fun scheduleAutoReconnect(device: BluetoothDevice, name: String) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch(Dispatchers.Default) {
            delay(3000)
            if (_connectionState.value == BleConnectionState.Disconnected) {
                Log.d(TAG, "Attempting auto-reconnect to ${device.address}")
                mainHandler.post {
                    connectToGatt(device, name)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupVeilServices(gatt: BluetoothGatt) {
        val veilService = gatt.getService(BleContract.SERVICE_UUID)
        val alertChar = veilService?.getCharacteristic(BleContract.ALERT_CHAR_UUID)

        if (alertChar != null) {
            gatt.setCharacteristicNotification(alertChar, true)
            val descriptor = alertChar.getDescriptor(BleContract.CCCD_DESCRIPTOR_UUID)
            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
            }
            Log.d(TAG, "Subscribed to VEIL Alert characteristic")
        }

        val batteryService = gatt.getService(BleContract.BATTERY_SERVICE_UUID)
        val batteryChar = batteryService?.getCharacteristic(BleContract.BATTERY_LEVEL_CHAR_UUID)
        if (batteryChar != null) {
            gatt.readCharacteristic(batteryChar)
        }

        val hrService = gatt.getService(BleContract.HEART_RATE_SERVICE_UUID)
        val hrChar = hrService?.getCharacteristic(BleContract.HEART_RATE_MEASUREMENT_CHAR_UUID)
        if (hrChar != null) {
            gatt.setCharacteristicNotification(hrChar, true)
            val hrDescriptor = hrChar.getDescriptor(BleContract.CCCD_DESCRIPTOR_UUID)
            if (hrDescriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(hrDescriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    hrDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(hrDescriptor)
                }
            }
            Log.d(TAG, "Subscribed to optional Heart Rate service")
        }
    }

    private fun handleCharacteristicData(uuidString: String, data: ByteArray?) {
        if (data == null || data.isEmpty()) return

        when {
            uuidString.equals(BleContract.ALERT_CHAR_UUID.toString(), ignoreCase = true) -> {
                val firstByte = data[0].toInt()
                Log.d(TAG, "Received Alert notification byte: $firstByte")
                if (firstByte == 1) {
                    scope.launch {
                        _alertTriggerEvents.emit("Wristband button")
                    }
                }
            }

            uuidString.equals(BleContract.BATTERY_LEVEL_CHAR_UUID.toString(), ignoreCase = true) -> {
                val level = data[0].toInt() and 0xFF
                _batteryPercent.value = level
                Log.d(TAG, "Read battery level: $level%")
            }

            uuidString.equals(BleContract.HEART_RATE_MEASUREMENT_CHAR_UUID.toString(), ignoreCase = true) -> {
                val flags = data[0].toInt()
                val is16Bit = (flags and 0x01) != 0
                val bpm = if (is16Bit && data.size >= 3) {
                    (data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8)
                } else if (data.size >= 2) {
                    data[1].toInt() and 0xFF
                } else 0

                if (bpm in 30..240) {
                    _heartRateBpm.value = bpm
                    val now = System.currentTimeMillis()
                    val currentList = _heartRateHistory.value.toMutableList()
                    currentList.add(HeartRatePoint(timestamp = now, bpm = bpm))
                    if (currentList.size > 60) currentList.removeAt(0)
                    _heartRateHistory.value = currentList
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnectGatt() {
        try {
            activeGatt?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing gatt", e)
        }
        activeGatt = null
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        reconnectJob?.cancel()
        autoReconnectDevice = null
        stopScanning()
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting gatt", e)
        }
        activeGatt = null
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
}
