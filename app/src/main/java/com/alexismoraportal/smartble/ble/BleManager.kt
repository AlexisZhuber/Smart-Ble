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
import android.util.Log
import com.alexismoraportal.smartble.BleForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * BleManager handles all Bluetooth Low Energy (BLE) operations, including:
 * - Scanning for BLE devices.
 * - Connecting to and disconnecting from a BLE device.
 * - Sending commands to the connected device.
 * - Receiving notifications with sensor data.
 *
 * This manager filters discovered devices to only include those with the name "SmartBleDevice".
 * The BLE service and characteristic used for communication are identified by predefined UUIDs.
 *
 * Incoming sensor data is expected in the format "D:<digital>,A:<analog>" and is parsed accordingly.
 * Any errors (e.g. connection failures, service discovery issues) are exposed via an errorMessage flow.
 *
 * When a device is connected, this manager also starts a foreground service (BleForegroundService)
 * so that sensor data continues to be displayed in a persistent notification even if the app
 * is sent to the background.
 *
 * Make sure to call [cleanup()] when the manager is no longer needed to unregister listeners and prevent memory leaks.
 *
 * @property context The application context used for BLE operations.
 */
class BleManager(private val context: Context) {

    // Obtain the default Bluetooth adapter (returns null if BLE is not supported)
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Holds the active BluetoothGatt connection (this manager supports one connection at a time)
    private var bluetoothGatt: BluetoothGatt? = null

    // Flow to hold a list of discovered BLE devices (each represented by a DeviceInfo object)
    private val _scanResults = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val scanResults = _scanResults.asStateFlow()

    // Flow representing whether a BLE device is currently connected
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    // Flow storing incoming sensor data (keyed by device address)
    private val _incomingMessages = MutableStateFlow<Map<String, SensorData>>(emptyMap())
    val incomingMessages = _incomingMessages.asStateFlow()

    // Flow for error messages (null if no error)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Flow representing the current Bluetooth enabled state
    private val _bluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val bluetoothEnabled = _bluetoothEnabled.asStateFlow()

    // Flow storing the address of the currently connected device (null if none)
    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress = _connectedDeviceAddress.asStateFlow()

    // Reference to the BLE scan callback (to stop scanning when needed)
    private var scanningCallback: ScanCallback? = null

    /**
     * BroadcastReceiver to monitor changes in Bluetooth state.
     * Updates the _bluetoothEnabled flow when the Bluetooth adapter's state changes.
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
        // Register the Bluetooth state receiver to listen for state changes.
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)
    }

    /**
     * Starts scanning for BLE devices using the system BLE scanner.
     * Only devices whose name equals "SmartBleDevice" are added to the scan results.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val device = result.device
                // Only include devices with the expected name.
                if (device.name == "SmartBleDevice") {
                    val deviceInfo = DeviceInfo(
                        name = device.name ?: "Unknown Device",
                        address = device.address
                    )
                    // Add device to the list if not already present.
                    if (_scanResults.value.none { it.address == deviceInfo.address }) {
                        _scanResults.value = _scanResults.value + deviceInfo
                        Log.d("BleManager", "Device found: ${deviceInfo.name} (${deviceInfo.address})")
                    }
                }
            }
        }
        scanningCallback = callback
        scanner.startScan(callback)
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
     * For stability, this implementation forces autoConnect to false on all devices.
     * Upon successful connection, the foreground service (BleForegroundService) is started to
     * display sensor data in a persistent notification.
     *
     * @param address The MAC address of the target BLE device.
     * @return true if the connection initiation succeeded, false otherwise.
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String): Boolean {
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return false
        // Force autoConnect = false for stability across Android versions.
        val autoConnect = false
        bluetoothGatt = device.connectGatt(context, autoConnect, gattCallback)
        Log.d("BleManager", "Initiating connection to device: $address, autoConnect=$autoConnect")
        return bluetoothGatt != null
    }

    /**
     * Sends a string command to the connected BLE device.
     * The string is converted to a UTF-8 ByteArray and written to the BLE characteristic.
     *
     * @param valueToSend The command string to send (e.g., "*100,255,0,0.").
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun sendValues(valueToSend: String) {
        val service = bluetoothGatt?.getService(UUID.fromString(SERVICE_UUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
        characteristic?.let {
            it.value = valueToSend.toByteArray(Charsets.UTF_8)
            bluetoothGatt?.writeCharacteristic(it)
            Log.d("BleManager", "Sent value: $valueToSend")
        }
    }

    /**
     * Disconnects from the connected BLE device and stops the foreground service.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        // Stop the foreground service since the device is disconnected.
        val serviceIntent = Intent(context, BleForegroundService::class.java)
        context.stopService(serviceIntent)
        Log.d("BleManager", "Disconnected from device")
    }

    /**
     * BluetoothGattCallback handles connection events, service discovery, and characteristic notifications.
     * It also starts or stops the foreground service based on the connection state and sends sensor data
     * updates via local broadcasts.
     */
    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _errorMessage.value = null
                    gatt?.apply {
                        requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        // Try with default MTU first
                        requestMtu(23)
                        Handler(Looper.getMainLooper()).postDelayed({ discoverServices() }, 1000)
                    }
                    _isConnected.value = true
                    _connectedDeviceAddress.value = gatt?.device?.address

                    // Start foreground service if needed
                    val serviceIntent = Intent(context, BleForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        _errorMessage.value = "Connection failed with status $status"
                        Log.e("BleManager", "Connection failed with status $status, retrying in 3 seconds...")
                        // Retry connection after 3 seconds if a device address is known
                        _connectedDeviceAddress.value?.let { address ->
                            Handler(Looper.getMainLooper()).postDelayed({
                                connectToDevice(address)
                            }, 3000)
                        }
                    }
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                    _isConnected.value = false
                    _connectedDeviceAddress.value = null

                    // Stop the foreground service
                    val serviceIntent = Intent(context, BleForegroundService::class.java)
                    context.stopService(serviceIntent)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                if (characteristic != null) {
                    // Enable notifications on the characteristic.
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    )
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    } else {
                        _errorMessage.value = "Descriptor not found on characteristic"
                        Log.e("BleManager", "Descriptor not found on characteristic")
                    }
                } else {
                    _errorMessage.value = "Characteristic not found on device"
                    Log.e("BleManager", "Characteristic not found on device")
                }
            } else {
                _errorMessage.value = "Service discovery failed with status $status"
                Log.e("BleManager", "Service discovery failed with status $status")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (characteristic?.uuid == UUID.fromString(CHARACTERISTIC_UUID)) {
                val message = characteristic?.getStringValue(0) ?: ""
                Log.d("BleManager", "Characteristic changed: $message")
                // Expect sensor data in format "D:<digital>,A:<analog>"
                if (message.startsWith("D:") && message.contains(",A:")) {
                    try {
                        val parts = message.split(",A:")
                        val digital = parts[0].removePrefix("D:").trim().toInt()
                        val analog = parts[1].trim().toInt()
                        // Update sensor data state
                        val sensorData = SensorData(digital, analog)
                        val deviceAddress = gatt?.device?.address ?: ""
                        _incomingMessages.value = _incomingMessages.value.toMutableMap().apply {
                            put(deviceAddress, sensorData)
                        }
                        // Create a broadcast to update the foreground notification with new sensor data.
                        val updateIntent = Intent(BleForegroundService.ACTION_UPDATE_SENSOR).apply {
                            putExtra(BleForegroundService.EXTRA_SENSOR_DATA, "D:$digital, A:$analog")
                            // Make the broadcast explicit by setting the package.
                            setPackage(context.packageName)
                        }
                        context.sendBroadcast(updateIntent)
                        Log.d("BleManager", "Sensor data broadcast sent: D:$digital, A:$analog")
                    } catch (e: Exception) {
                        _errorMessage.value = "Error parsing sensor data: ${e.message}"
                        Log.e("BleManager", "Error parsing sensor data", e)
                    }
                }
            }
        }
    }

    /**
     * Unregisters the Bluetooth state BroadcastReceiver to avoid memory leaks.
     */
    fun cleanup() {
        try {
            context.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered; ignore the exception.
            Log.w("BleManager", "Bluetooth state receiver was not registered")
        }
    }

    companion object {
        // UUID for the BLE service provided by the peripheral.
        const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        // UUID for the BLE characteristic used for communication.
        const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    }
}
