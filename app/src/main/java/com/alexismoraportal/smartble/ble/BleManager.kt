package com.alexismoraportal.smartble.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * BleManager handles all Bluetooth Low Energy (BLE) operations including:
 * - Scanning for BLE devices
 * - Connecting and disconnecting to/from a BLE device
 * - Sending data/commands to the connected device
 * - Receiving notifications from the device
 * - Enabling Bluetooth programmatically if necessary
 *
 * Only devices with the exact name "SmartBleDevice" are added to the scan results.
 * The BLE service and characteristic used for communication are identified by predefined UUIDs.
 * Incoming sensor data is expected in the format "D:<digital>,A:<analog>" and is parsed accordingly.
 *
 * All errors (such as connection or service discovery failures) are exposed through the errorMessage flow.
 *
 * Note: The enableBluetooth() method uses BluetoothAdapter.enable() which is asynchronous and may not
 * work on newer Android versions without user interaction. It is recommended to request the user
 * to enable Bluetooth via a system dialog when possible.
 *
 * Additionally, a cleanup() method is provided to unregister the BroadcastReceiver and avoid memory leaks.
 *
 * @property context The application context used for creating BLE connections.
 */
class BleManager(private val context: Context) {

    // Retrieve the default Bluetooth adapter (null if unsupported).
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Holds the GATT connection to the BLE device (only one connection supported at a time).
    private var bluetoothGatt: BluetoothGatt? = null

    // Flow of discovered BLE devices (DeviceInfo objects).
    private val _scanResults = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val scanResults = _scanResults.asStateFlow()

    // Flow representing whether a BLE device is connected (true) or not (false).
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    // Flow storing incoming sensor data, keyed by device address.
    private val _incomingMessages = MutableStateFlow<Map<String, SensorData>>(emptyMap())
    val incomingMessages = _incomingMessages.asStateFlow()

    // Flow for error messages (null if no error).
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Flow representing the current Bluetooth enabled state (updated via BroadcastReceiver).
    private val _bluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val bluetoothEnabled = _bluetoothEnabled.asStateFlow()

    // Flow storing the address of the currently connected device (null if none).
    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress = _connectedDeviceAddress.asStateFlow()

    // Reference to the BLE scan callback for stopping scans later.
    private var scanningCallback: ScanCallback? = null

    /**
     * BroadcastReceiver that updates the _bluetoothEnabled flow when the Bluetooth state changes.
     */
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                _bluetoothEnabled.value = (state == BluetoothAdapter.STATE_ON)
            }
        }
    }

    init {
        // Register a BroadcastReceiver to listen for Bluetooth state changes.
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)
    }

    /**
     * Starts scanning for BLE devices using the system Bluetooth LE scanner.
     * Only devices named "SmartBleDevice" are added to the results.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        bluetoothAdapter?.bluetoothLeScanner?.let { scanner ->
            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)
                    val device = result.device
                    if (device.name == "SmartBleDevice") {
                        val deviceInfo = DeviceInfo(name = device.name, address = device.address)
                        if (_scanResults.value.none { it.address == deviceInfo.address }) {
                            _scanResults.value += deviceInfo
                        }
                    }
                }
            }
            scanningCallback = callback
            scanner.startScan(callback)
        }
    }

    /**
     * Stops scanning for BLE devices.
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanningCallback)
        scanningCallback = null
    }

    /**
     * Connects to a BLE device using its MAC address.
     *
     * For Android 12 (API 31) devices, autoConnect = true for improved stability;
     * for other versions, autoConnect = false for a direct connection.
     *
     * @param address The MAC address of the BLE device.
     * @return true if the connection initiation succeeded, false otherwise.
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String): Boolean {
        val device = bluetoothAdapter?.getRemoteDevice(address)
        val autoConnect = (Build.VERSION.SDK_INT == Build.VERSION_CODES.S)
        bluetoothGatt = device?.connectGatt(context, autoConnect, gattCallback)
        return bluetoothGatt != null
    }

    /**
     * Sends a string command to the connected BLE device by writing a UTF-8 ByteArray to the characteristic.
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun sendValues(valueToSend: String) {
        val service = bluetoothGatt?.getService(UUID.fromString(SERVICE_UUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
        characteristic?.let {
            it.value = valueToSend.toByteArray(Charsets.UTF_8)
            bluetoothGatt?.writeCharacteristic(it)
        }
    }

    /**
     * Disconnects from the currently connected BLE device.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    /**
     * BluetoothGattCallback for connection events, service discovery, and notifications.
     */
    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _errorMessage.value = null
                    gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt?.requestMtu(512)
                    Handler(Looper.getMainLooper()).postDelayed({
                        gatt?.discoverServices()
                    }, 300)
                    _isConnected.value = true

                    // Store the connected device address.
                    _connectedDeviceAddress.value = gatt?.device?.address
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        _errorMessage.value = "Connection failed with status $status"
                    }
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                    _isConnected.value = false

                    // Clear the connected device address on disconnection.
                    _connectedDeviceAddress.value = null
                }
            }
        }

        @Suppress("DEPRECATION")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    )
                    descriptor?.let {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(it)
                    } ?: run {
                        _errorMessage.value = "Descriptor not found on characteristic"
                    }
                } else {
                    _errorMessage.value = "Characteristic not found on device"
                }
            } else {
                _errorMessage.value = "Service discovery failed with status $status"
            }
        }

        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if (characteristic?.uuid == UUID.fromString(CHARACTERISTIC_UUID)) {
                val message = characteristic?.getStringValue(0) ?: ""
                if (message.startsWith("D:") && message.contains(",A:")) {
                    try {
                        val parts = message.split(",A:")
                        val digital = parts[0].removePrefix("D:").trim().toInt()
                        val analog = parts[1].trim().toInt()
                        val sensorData = SensorData(digital, analog)
                        val deviceAddress = gatt?.device?.address ?: ""
                        _incomingMessages.value = _incomingMessages.value.toMutableMap().apply {
                            put(deviceAddress, sensorData)
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "Error parsing sensor data: ${e.message}"
                    }
                }
            }
        }
    }

    /**
     * Cleanup method to unregister the Bluetooth state BroadcastReceiver and avoid memory leaks.
     */
    fun cleanup() {
        try {
            context.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered; ignore the exception.
        }
    }

    companion object {
        // UUID for the BLE service provided by the peripheral.
        const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        // UUID for the BLE characteristic used for communication.
        const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    }
}
