package com.example.wish_list.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wish_list.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

class RemoteConfigSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            remoteConfig.setConfigSettingsAsync(settings).await()
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await()
            remoteConfig.fetchAndActivate().await()
            Log.d(TAG, "RemoteConfig sync succeeded")
            Result.success()
        }.getOrElse { error ->
            Log.w(TAG, "RemoteConfig sync failed", error)
            Result.retry()
        }
    }

    private companion object {
        private const val TAG = "RemoteConfigSyncWorker"
    }
}
