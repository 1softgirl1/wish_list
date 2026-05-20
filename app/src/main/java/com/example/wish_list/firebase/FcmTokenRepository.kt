package com.example.wish_list.firebase

import android.content.Context
import com.example.wish_list.auth.SecureSessionStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FcmTokenRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureSessionStore: SecureSessionStore,
    private val userProfileRepository: UserProfileRepository
) {
    fun saveToken(token: String) {
        val session = secureSessionStore.load() ?: return
        userProfileRepository.updateFcmToken(session.userId, token)
    }
}
