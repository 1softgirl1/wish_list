package com.example.wish_list.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSessionStore(context: Context) {
    private val preferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(session: AuthSession) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, session.token)
            .putString(KEY_USER_NAME, session.userName)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_PROVIDER, session.provider.name)
            .putLong(KEY_EXPIRES_AT, session.expiresAtMillis)
            .apply()
    }

    fun load(): AuthSession? {
        val token = preferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val userName = preferences.getString(KEY_USER_NAME, null) ?: return null
        val storedUserId = preferences.getString(KEY_USER_ID, null)
        val email = preferences.getString(KEY_EMAIL, null)
        val provider = preferences.getString(KEY_PROVIDER, null) ?: return null
        val expiresAtMillis = preferences.getLong(KEY_EXPIRES_AT, 0L)
        val parsedProvider = runCatching { AuthProvider.valueOf(provider) }.getOrNull() ?: return null
        val userId = storedUserId ?: "${parsedProvider.name.lowercase()}_${token.take(12)}"
        return AuthSession(
            token = token,
            userName = userName,
            userId = userId,
            email = email,
            provider = parsedProvider,
            expiresAtMillis = expiresAtMillis
        )
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_PROVIDER)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    companion object {
        private const val FILE_NAME = "secure_auth_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
