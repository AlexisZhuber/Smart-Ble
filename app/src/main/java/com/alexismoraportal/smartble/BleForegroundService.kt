package com.alexismoraportal.smartble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

/**
 * BleForegroundService is a Foreground Service that continuously displays sensor data
 * received from a BLE device in a persistent notification. The service listens for local
 * broadcasts with sensor updates and refreshes the notification accordingly.
 *
 * This service creates a notification channel (required for Android Oreo and above) and starts
 * in foreground mode. It registers a BroadcastReceiver (with the flag RECEIVER_NOT_EXPORTED)
 * so that it only receives broadcasts from within the same application.
 *
 * **Important:** In Android 12 and above, the service must declare a foreground service type
 * (set in the manifest as android:foregroundServiceType="connectedDevice").
 */
class BleForegroundService : Service() {

    // A CoroutineScope used for any background tasks in the service.
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // BroadcastReceiver that listens for sensor data updates.
    private lateinit var sensorUpdateReceiver: BroadcastReceiver

    companion object {
        // Notification channel ID and notification ID for the persistent notification.
        const val NOTIFICATION_CHANNEL_ID = "ble_channel"
        const val NOTIFICATION_ID = 1

        // Constants for the action and extra used in sensor update broadcasts.
        const val ACTION_UPDATE_SENSOR = "com.alexismoraportal.smartble.UPDATE_SENSOR"
        const val EXTRA_SENSOR_DATA = "sensorData"
    }

    /**
     * Called when the service is created.
     *
     * It creates the notification channel, starts the service in the foreground with an initial
     * notification, and registers the BroadcastReceiver to listen for sensor updates.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Start the service in the foreground with an initial message.
        startForeground(NOTIFICATION_ID, buildNotification("Initializing sensor data..."))
        registerSensorUpdateReceiver()
    }

    /**
     * Registers a BroadcastReceiver that listens for sensor data updates.
     *
     * When a broadcast with the action ACTION_UPDATE_SENSOR is received, the notification is updated.
     * The receiver is registered with the flag Context.RECEIVER_NOT_EXPORTED to ensure that it
     * only receives broadcasts from within this app.
     */
    private fun registerSensorUpdateReceiver() {
        val filter = IntentFilter(ACTION_UPDATE_SENSOR)
        sensorUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Extract the sensor data from the intent extras.
                val sensorData = intent?.getStringExtra(EXTRA_SENSOR_DATA) ?: return
                updateSensorData(sensorData)
                Log.d("BleForegroundService", "Broadcast received with sensor data: $sensorData")
            }
        }
        // Register the receiver with the flag RECEIVER_NOT_EXPORTED.
        registerReceiver(sensorUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    /**
     * Called when the service is started. Returns START_STICKY to ensure the service is restarted
     * if it is terminated by the system.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * This service does not support binding, so return null.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Creates a notification channel for the foreground service.
     *
     * This is required for Android Oreo (API 26) and above. The channel is created with
     * IMPORTANCE_DEFAULT to ensure the notification is sufficiently visible, but with sound and
     * vibration disabled.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "BLE Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Disable sound and vibration for this channel.
            channel.setSound(null, null)
            channel.enableVibration(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and returns a Notification displaying the provided sensor data.
     *
     * @param sensorData A String representing the sensor values.
     * @return A Notification instance.
     */
    private fun buildNotification(sensorData: String): Notification {
        // Intent to launch MainActivity when the notification is tapped.
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sensor Data")
            .setContentText(sensorData)
            .setSmallIcon(R.drawable.bluetooth)  // Ensure that R.drawable.logo exists and is valid.
            .setContentIntent(pendingIntent)
            // Disable sound and vibration for the notification.
            .setSound(null)
            .setVibrate(longArrayOf(0L))
            // Mark the notification as ongoing so it cannot be dismissed manually.
            .setOngoing(true)
            // Ensure that the notification only alerts once.
            .setOnlyAlertOnce(true)
            .build()
    }

    /**
     * Updates the persistent notification with new sensor data.
     *
     * This method rebuilds the notification with the new sensor data and updates it.
     *
     * @param sensorData A String representing the latest sensor values.
     */
    @SuppressLint("MissingPermission")
    fun updateSensorData(sensorData: String) {
        val notification = buildNotification(sensorData)
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Called when the service is destroyed.
     *
     * Cancels any running coroutines and unregisters the BroadcastReceiver to avoid memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancel any running coroutines.
        unregisterReceiver(sensorUpdateReceiver)
    }
}
