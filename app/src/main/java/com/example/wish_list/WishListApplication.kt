package com.example.wish_list

import android.app.Application
import android.util.Log
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import com.vk.id.VKID
import com.yandex.mapkit.MapKitFactory

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
    }

    private fun String.maskForLog(): String {
        if (isBlank()) return "<empty>"
        if (length <= 8) return "*".repeat(length)
        return "${take(4)}***${takeLast(4)}"
    }
}
