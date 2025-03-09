package com.alexismoraportal.smartble.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.ble.BleViewModel
import com.alexismoraportal.smartble.ble.SensorData
import com.alexismoraportal.smartble.ui.theme.BackgroundLight
import com.alexismoraportal.smartble.ui.theme.Primary

/**
 * HomeScreen serves as the main connection and control interface.
 *
 * When not connected, it displays a scanning UI with a toggle button and a list of discovered devices.
 * When a device is connected, it shows the control UI including:
 *   - A Disconnect button.
 *   - The connected device's address.
 *   - Sensor data (or an error message).
 *   - A ColorPicker to send commands.
 *
 * The ColorPicker’s state is persisted by hoisting its controller outside the composable so that the same
 * instance is reused even after navigation, preventing the ColorPicker from resetting to its default values.
 *
 * @param bleViewModel The BleViewModel that manages BLE operations and holds connection state.
 */
@Composable
fun HomeScreen(bleViewModel: BleViewModel = hiltViewModel()) {
    // Observe BLE states from the ViewModel.
    val scanResults by bleViewModel.scanResults.collectAsState()
    val isConnected by bleViewModel.isConnected.collectAsState()
    val bluetoothEnabled by bleViewModel.bluetoothEnabled.collectAsState()
    val connectedDeviceAddress by bleViewModel.connectedDeviceAddress.collectAsState()

    // Local state for toggling scanning mode.
    var isScanning by remember { mutableStateOf(false) }

    // If Bluetooth is not enabled, display a full-screen message.
    if (!bluetoothEnabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.bluetooth_must_be_enabled),
                color = Primary,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        return
    }

    // If a device is connected, display the control UI.
    if (isConnected && connectedDeviceAddress != null) {
        // Retrieve sensor data and error message states.
        val sensorDataMap by bleViewModel.incomingMessages.collectAsState(initial = emptyMap())
        val sensorData: SensorData? = sensorDataMap[connectedDeviceAddress]
        val errorMessage by bleViewModel.errorMessage.collectAsState(initial = null)

        ConnectedDeviceScreen(
            bleViewModel = bleViewModel,
            connectedDeviceAddress = connectedDeviceAddress.toString(),
            sensorData = sensorData,
            errorMessage = errorMessage,
            onDisconnect = {
                bleViewModel.disconnect()
                isScanning = false
            }
        )
    } else {
        // When no device is connected, display the scanning UI.
        ScanningScreen(
            bleViewModel = bleViewModel,
            isScanning = isScanning,
            scanResults = scanResults,
            onToggleScan = {
                if (isScanning) {
                    bleViewModel.stopScan()
                } else {
                    bleViewModel.startScan()
                }
                isScanning = !isScanning
            }
        )
    }
}
