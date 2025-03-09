package com.alexismoraportal.smartble

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * BleApplication initializes Hilt for dependency injection.
 */
@HiltAndroidApp
class SmartBleApplication : Application()
