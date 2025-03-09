package com.alexismoraportal.smartble.di

import android.content.Context
import com.alexismoraportal.smartble.ble.BleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule provides BLE-related dependencies using Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideBleManager(@ApplicationContext context: Context): BleManager {
        return BleManager(context)
    }
}
