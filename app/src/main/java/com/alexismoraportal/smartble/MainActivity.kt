package com.alexismoraportal.smartble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.alexismoraportal.smartble.navigation.NavManager
import com.alexismoraportal.smartble.ui.theme.SmartBLETheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity is the entry point of the application.
 * It requests the necessary runtime permissions for BLE scanning and connection,
 * including location permission for Android <= 11, as well as the POST_NOTIFICATIONS
 * permission for Android 13+ if not already granted. Then it sets up the Compose UI
 * and launches the NavManager.
 *
 * Additionally, it checks whether Bluetooth is enabled and, if not, prompts the user to enable it
 * via a system dialog using an Activity Result Launcher.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Launcher for enabling Bluetooth via system dialog.
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("MainActivity", "Bluetooth has been enabled by the user.")
        } else {
            Log.w("MainActivity", "Bluetooth was not enabled by the user.")
            // Optionally, you can redirect to app settings or show a message.
        }
    }

    // Launcher for requesting multiple permissions.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.entries.all { it.value }
        if (!allPermissionsGranted) {
            // If not all permissions are granted, redirect the user to the app settings.
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request BLE-related permissions on startup.
        checkAndRequestPermissions()

        // Check if Bluetooth is enabled; if not, request the user to enable it.
        requestEnableBluetooth()

        setContent {
            SmartBLETheme {
                NavManager()
            }
        }
    }

    /**
     * Requests runtime permissions for BLE scanning and connection.
     * - On Android 12+ (API 31+), it requests BLUETOOTH_SCAN and BLUETOOTH_CONNECT.
     * - On Android <= 11, it requests ACCESS_FINE_LOCATION (often needed to discover BLE).
     * - On Android 13+ (API 33+), it requests POST_NOTIFICATIONS to show notifications.
     */
    private fun checkAndRequestPermissions() {
        val neededPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = neededPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }

    /**
     * Checks whether Bluetooth is enabled, and if not, launches a system dialog to request enabling it.
     *
     * This function uses an ActivityResultLauncher to start the intent and receive the result.
     */
    private fun requestEnableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }
}
