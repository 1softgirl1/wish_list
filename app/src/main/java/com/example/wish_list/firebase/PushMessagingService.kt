package com.example.wish_list.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToPreferences(token)
        Log.d(TAG, "New FCM token: $token")
        FcmTokenRepository(applicationContext).saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notificationHelper = AppNotificationHelper(applicationContext)
        notificationHelper.ensureChannel()
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Wish List update"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Open app to see details"
        Log.d(TAG, "Push received: title=$title body=$body data=${message.data}")
        notificationHelper.showPushNotification(
            title = title,
            body = body,
            payload = message.data
        )
    }

    private fun saveTokenToPreferences(token: String) {
        val preferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }

    private companion object {
        private const val TAG = "PushMessagingService"
        private const val PREFS_NAME = "push_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
}
