package com.alexismoraportal.smartble.ble

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining BLE operations for scanning, connecting,
 * sending, and receiving data.
 */
interface IBleManager {
    val scanResults: StateFlow<List<DeviceInfo>>
    val isConnected: StateFlow<Map<String, Boolean>>
    val incomingMessages: StateFlow<Map<String, SensorData>>

    fun startScan()
    fun stopScan()
    fun connectToDevice(address: String): Boolean
    fun disconnectDevice(address: String)
    fun sendValues(address: String, value: String)
    fun sendValuesToAll(value: String)
    fun cleanup()
}
