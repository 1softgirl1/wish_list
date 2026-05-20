package com.example.wish_list.firebase

import android.util.Log
import com.example.wish_list.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class RemoteConfigSnapshot(
    val greetingText: String,
    val isFirestoreRealtimeEnabled: Boolean,
    val maxItemsPerPage: Long
)

class FirebaseRemoteConfigService @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigService {
    override suspend fun fetchAndActivate(isDebug: Boolean): RemoteConfigSnapshot {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (isDebug) 0 else 3600)
            .build()

        remoteConfig.setConfigSettingsAsync(settings).await()
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await()
        runCatching { remoteConfig.fetchAndActivate().await() }
            .onFailure { Log.w(TAG, "Remote Config fetch failed. Using defaults.", it) }

        return RemoteConfigSnapshot(
            greetingText = remoteConfig.getString(RemoteConfigKeys.KEY_GREETING),
            isFirestoreRealtimeEnabled = remoteConfig.getBoolean(RemoteConfigKeys.KEY_IS_NEW_FEATURE_ENABLED),
            maxItemsPerPage = remoteConfig.getLong(RemoteConfigKeys.KEY_MAX_ITEMS_PER_PAGE)
        )
    }

    private companion object {
        private const val TAG = "FirebaseRemoteConfigService"
    }
}
