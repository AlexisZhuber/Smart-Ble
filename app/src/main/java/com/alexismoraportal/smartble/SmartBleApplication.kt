package com.alexismoraportal.smartble

import android.app.Application
import com.alexismoraportal.smartble.ble.BleManager
import dagger.hilt.android.HiltAndroidApp

/**
 * BleApplication initializes Hilt for dependency injection.
 */
@HiltAndroidApp
class SmartBleApplication : Application(){
    val bleManager: BleManager by lazy { BleManager(applicationContext) }
}
