package com.example.wish_list.firebase

import android.content.Context
import com.example.wish_list.auth.SecureSessionStore

class FcmTokenRepository(
    private val context: Context,
    private val secureSessionStore: SecureSessionStore = SecureSessionStore(context),
    private val userProfileRepository: UserProfileRepository = UserProfileRepository()
) {
    fun saveToken(token: String) {
        val session = secureSessionStore.load() ?: return
        userProfileRepository.updateFcmToken(session.userId, token)
    }
}
