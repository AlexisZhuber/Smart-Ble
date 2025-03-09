package com.alexismoraportal.smartble.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.ble.BleViewModel
import com.alexismoraportal.smartble.ble.DeviceInfo
import com.alexismoraportal.smartble.components.AppButton
import com.alexismoraportal.smartble.ui.theme.BackgroundDark
import com.alexismoraportal.smartble.ui.theme.Primary
import com.alexismoraportal.smartble.ui.theme.TextPrimaryDark

/**
 * ScanningScreen displays the BLE scanning UI.
 *
 * It shows:
 * - A button to start/stop scanning.
 * - A progress indicator when scanning.
 * - A list of discovered devices filtered by name.
 *
 * @param bleViewModel The BleViewModel for BLE operations.
 * @param isScanning Boolean flag indicating whether scanning is in progress.
 * @param scanResults List of discovered BLE devices.
 * @param onToggleScan Callback to start or stop scanning.
 */
@Composable
fun ScanningScreen(
    bleViewModel: BleViewModel,
    isScanning: Boolean,
    scanResults: List<DeviceInfo>,
    onToggleScan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.connect_devices),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(24.dp))
            AppButton(
                text = if (isScanning) stringResource(id = R.string.stop_scan) else stringResource(id = R.string.start_scan),
                onClick = onToggleScan,
                icon = if (!isScanning) Icons.Filled.PlayArrow else Icons.Filled.Stop
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    color = Primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (scanResults.isEmpty()) {
                    Text(
                        text = "Searching for devices...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimaryDark
                    )
                }
            } else if (scanResults.isEmpty()) {
                Text(
                    text = "No devices found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimaryDark
                )
            }
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(scanResults.filter { it.name == "SmartBleDevice" }) { device ->
                    DeviceItem(
                        device = device,
                        onClick = {
                            bleViewModel.connectToDevice(device.address)
                        }
                    )
                }
            }
        }
    }
}
