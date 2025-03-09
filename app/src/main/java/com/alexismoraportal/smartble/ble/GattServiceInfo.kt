package com.alexismoraportal.smartble.ble

/**
 * Represents a discovered GATT service with its characteristics.
 */
data class GattServiceInfo(
    val serviceUuid: String,
    val characteristics: List<GattCharacteristicInfo>
)

/**
 * Represents a discovered GATT characteristic with its properties and descriptors.
 */
data class GattCharacteristicInfo(
    val characteristicUuid: String,
    val properties: String,
    val descriptors: List<GattDescriptorInfo>
)

/**
 * Represents a discovered GATT descriptor.
 */
data class GattDescriptorInfo(
    val descriptorUuid: String,
    val value: String?
)
