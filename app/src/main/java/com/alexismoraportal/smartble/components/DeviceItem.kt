package com.alexismoraportal.smartble.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.ble.DeviceInfo
import com.alexismoraportal.smartble.ui.theme.Tertiary
import com.alexismoraportal.smartble.ui.theme.TextPrimaryDark

/**
 * DeviceItem is a composable representing a single BLE device in the list.
 *
 * @param device The BLE device information.
 * @param onClick Callback to be invoked when the device item is clicked.
 */
@Composable
fun DeviceItem(
    device: DeviceInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = device.name ?: "SmartBleDevice",
            color = Tertiary,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = device.address,
            color = TextPrimaryDark,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
