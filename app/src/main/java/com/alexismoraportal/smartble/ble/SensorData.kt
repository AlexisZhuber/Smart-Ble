package com.alexismoraportal.smartble.ble

/**
 * Data class representing sensor data received from a BLE device.
 *
 * @property digital The digital signal reading (e.g., push button state).
 * @property analog The analog signal reading (e.g., potentiometer value).
 */
data class SensorData(
    val digital: Int,
    val analog: Int
)
