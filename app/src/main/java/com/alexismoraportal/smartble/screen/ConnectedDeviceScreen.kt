package com.alexismoraportal.smartble.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.ble.BleViewModel
import com.alexismoraportal.smartble.ble.SensorData
import com.alexismoraportal.smartble.components.AppButton
import com.alexismoraportal.smartble.components.ColorPicker
import com.alexismoraportal.smartble.ui.theme.BackgroundDark
import com.alexismoraportal.smartble.ui.theme.Primary
import com.alexismoraportal.smartble.ui.theme.TextPrimaryDark

/**
 * ConnectedDeviceScreen displays the control interface when a device is connected.
 *
 * It shows:
 * - A Disconnect button.
 * - The connected device's address.
 * - Sensor data (or an error message).
 * - A ColorPicker to send commands.
 *
 * @param bleViewModel The BleViewModel for BLE operations.
 * @param connectedDeviceAddress The address of the connected BLE device.
 * @param sensorData The sensor data received from the connected device, if available.
 * @param errorMessage The error message to display, if any.
 * @param onDisconnect Callback to be invoked when the disconnect button is clicked.
 */
@Composable
fun ConnectedDeviceScreen(
    bleViewModel: BleViewModel,
    connectedDeviceAddress: String,
    sensorData: SensorData?,
    errorMessage: String?,
    onDisconnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterVertically)
        ) {
            // Disconnect button.
            AppButton(
                text = stringResource(id = R.string.disconnect),
                onClick = onDisconnect,
                icon = Icons.Filled.BluetoothDisabled
            )
            // Display the connected device address.
            Text(
                text = stringResource(id = R.string.device_address, connectedDeviceAddress),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimaryDark
            )
            // Display sensor data or error message.
            if (errorMessage != null) {
                Text(
                    text = stringResource(id = R.string.error_message, errorMessage),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                if (sensorData != null) {
                    Text(
                        text = stringResource(id = R.string.sensor_data, sensorData.digital, sensorData.analog),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimaryDark
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.no_sensor_data),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimaryDark
                    )
                }
            }
            // Display the ColorPicker to send commands.
            // The ColorPicker's controller is hoisted outside to persist its state.
            ColorPicker(
                onColorSelected = { colorCommand ->
                    bleViewModel.sendValues(colorCommand)
                }
            )
        }
    }
}
