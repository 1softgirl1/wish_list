package com.example.wish_list

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wish_list.firebase.AppNotificationHelper
import com.example.wish_list.work.RemoteConfigSyncWorker
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import com.vk.id.VKID
import com.yandex.mapkit.MapKitFactory
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class WishListApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val apiKey = BuildConfig.APPMETRICA_API_KEY
        if (apiKey.isNotBlank()) {
            val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
            AppMetrica.activate(this, config)
            AppMetrica.enableActivityAutoTracking(this)
        }

        val mapKitApiKey = BuildConfig.MAPKIT_API_KEY
        if (BuildConfig.DEBUG) {
            val maskedKey = mapKitApiKey.maskForLog()
            Log.d(
                "WishListApplication",
                "MapKit key present=${mapKitApiKey.isNotBlank()}, length=${mapKitApiKey.length}, masked=$maskedKey"
            )
        }
        if (mapKitApiKey.isNotBlank()) {
            runCatching {
                MapKitFactory.setApiKey(mapKitApiKey)
                MapKitFactory.initialize(this)
                if (BuildConfig.DEBUG) {
                    Log.d("WishListApplication", "MapKit initialized successfully")
                }
            }.onFailure {
                Log.e("WishListApplication", "MapKit initialization failed", it)
            }
        } else {
            Log.w("WishListApplication", "MAPKIT_API_KEY is empty, map features are disabled")
        }

        runCatching { VKID.init(this) }
            .onFailure { Log.e("WishListApplication", "VKID initialization failed", it) }

        AppNotificationHelper(this).ensureChannel()
        initializeRemoteConfig()
        schedulePeriodicRemoteConfigSync()
    }

    private fun String.maskForLog(): String {
        if (isBlank()) return "<empty>"
        if (length <= 8) return "*".repeat(length)
        return "${take(4)}***${takeLast(4)}"
    }

    private fun initializeRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun schedulePeriodicRemoteConfigSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val periodicWork = PeriodicWorkRequestBuilder<RemoteConfigSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            REMOTE_CONFIG_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }

    private companion object {
        private const val REMOTE_CONFIG_SYNC_WORK_NAME = "remote_config_sync"
    }
}
