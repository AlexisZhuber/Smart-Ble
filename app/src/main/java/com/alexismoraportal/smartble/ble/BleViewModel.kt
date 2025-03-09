package com.alexismoraportal.smartble.ble

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * BleViewModel serves as the bridge between the UI and low-level BLE operations.
 *
 * It manages scanning, connecting/disconnecting, sending commands, and receives sensor data.
 * The ViewModel exposes various StateFlows for the UI to observe:
 * - scanResults (List of DeviceInfo)
 * - isConnected (Boolean)
 * - incomingMessages (Map<String, SensorData>)
 * - errorMessage (Flow from BleManager)
 * - bluetoothEnabled (Flow from BleManager)
 * - connectedDeviceAddress (Flow from BleManager)
 *
 * Additionally, it provides a cleanup() method to free resources (e.g., BroadcastReceiver) in BleManager
 * when the ViewModel is destroyed.
 *
 * @param application The Application context used to create the BleManager.
 */
class BleViewModel(application: Application) : AndroidViewModel(application) {

    // The BleManager handling all BLE operations.
    private val bleManager = BleManager(application)

    // StateFlow holding the list of discovered BLE devices.
    private val _scanResults = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val scanResults = _scanResults.asStateFlow()

    // StateFlow representing the connection state (true if connected).
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    // StateFlow storing incoming sensor data from the connected device.
    private val _incomingMessages = MutableStateFlow<Map<String, SensorData>>(emptyMap())
    val incomingMessages = _incomingMessages.asStateFlow()

    // Error messages come directly from BleManager.
    val errorMessage = bleManager.errorMessage

    // Reactive Bluetooth enabled state from BleManager.
    val bluetoothEnabled = bleManager.bluetoothEnabled

    // Flow exposing the address of the currently connected device (if any).
    val connectedDeviceAddress = bleManager.connectedDeviceAddress

    init {
        // Collect scan results from BleManager and update the ViewModel's flow.
        viewModelScope.launch {
            bleManager.scanResults.collect { devices ->
                _scanResults.value = devices
            }
        }
        // Collect connection state from BleManager and update the ViewModel's flow.
        viewModelScope.launch {
            bleManager.isConnected.collect { connected ->
                _isConnected.value = connected
            }
        }
        // Collect incoming sensor data and update the ViewModel's flow.
        viewModelScope.launch {
            bleManager.incomingMessages.collect { messages ->
                _incomingMessages.value = messages
            }
        }
    }

    /**
     * Starts scanning for BLE devices. The results will appear in scanResults.
     */
    fun startScan() {
        viewModelScope.launch {
            bleManager.startScan()
        }
    }

    /**
     * Stops scanning for BLE devices.
     */
    fun stopScan() {
        viewModelScope.launch {
            bleManager.stopScan()
        }
    }

    /**
     * Connects to a BLE device by its MAC address.
     *
     * Stops scanning first, then attempts a connection.
     * If successful, clears the scan results and updates the isConnected flow.
     *
     * @param address The MAC address of the target BLE device.
     */
    fun connectToDevice(address: String) {
        stopScan()
        viewModelScope.launch {
            val result = bleManager.connectToDevice(address)
            if (result) {
                clearScanResults()
            }
            _isConnected.value = result
        }
    }

    /**
     * Sends a string command to the connected BLE device.
     *
     * The string is converted to UTF-8 bytes and written to the characteristic in BleManager.
     *
     * @param valueToSend The command string (e.g., "turn on", "change color").
     */
    fun sendValues(valueToSend: String) {
        viewModelScope.launch {
            bleManager.sendValues(valueToSend)
        }
    }

    /**
     * Disconnects from the currently connected BLE device.
     */
    fun disconnect() {
        viewModelScope.launch {
            bleManager.disconnect()
            _isConnected.value = false
        }
    }

    /**
     * Clears the list of scanned devices.
     */
    private fun clearScanResults() {
        _scanResults.value = emptyList()
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Cleans up resources in BleManager (e.g., BroadcastReceiver).
     */
    override fun onCleared() {
        super.onCleared()
        bleManager.cleanup()
    }
}
