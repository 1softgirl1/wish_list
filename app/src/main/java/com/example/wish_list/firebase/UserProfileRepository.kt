package com.example.wish_list.firebase

import android.util.Log
import com.example.wish_list.auth.AuthSession
import com.example.wish_list.domain.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val registration = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val dto = snapshot.toObject(FirestoreUserProfileDto::class.java)
                val activeExternalUserId = snapshot.getString(FIELD_ACTIVE_EXTERNAL_USER_ID).orEmpty()
                val providerProfile = snapshot.get(FIELD_PROFILES)?.let { raw ->
                    @Suppress("UNCHECKED_CAST")
                    (raw as? Map<String, Any?>)?.get(activeExternalUserId) as? Map<String, Any?>
                }
                val providerName = providerProfile?.get("name") as? String
                val providerEmail = providerProfile?.get("email") as? String
                val profile = UserProfile(
                    userId = userId,
                    name = providerName?.takeIf { it.isNotBlank() } ?: dto?.name.orEmpty(),
                    email = providerEmail?.takeIf { it.isNotBlank() } ?: dto?.email.orEmpty(),
                    fcmToken = dto?.fcmToken.orEmpty(),
                    updatedAtMillis = dto?.updatedAt?.time ?: 0L
                )
                trySend(profile)
            }
        awaitClose { registration.remove() }
    }

    fun saveOrUpdateProfile(documentUserId: String, session: AuthSession, fcmToken: String?) {
        val profileEntry = mapOf(
            "name" to session.userName,
            "email" to session.email.orEmpty(),
            "provider" to session.provider.name,
            "updatedAt" to Date()
        )
        val profile = FirestoreUserProfileDto(
            name = session.userName,
            email = session.email.orEmpty(),
            fcmToken = fcmToken.orEmpty(),
            updatedAt = Date()
        )
        val payload = hashMapOf<String, Any>(
            FIELD_NAME to profile.name,
            FIELD_EMAIL to profile.email,
            FIELD_UPDATED_AT to profile.updatedAt,
            FIELD_ACTIVE_EXTERNAL_USER_ID to session.userId,
            "$FIELD_PROFILES.${session.userId}" to profileEntry
        )
        if (!fcmToken.isNullOrBlank()) {
            payload[FIELD_FCM_TOKEN] = fcmToken
        }
        firestore.collection(COLLECTION_USERS)
            .document(documentUserId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Profile saved for $documentUserId") }
            .addOnFailureListener { e -> Log.w(TAG, "Error saving profile", e) }
    }

    fun updateFcmToken(userId: String, token: String) {
        val payload = mapOf<String, Any>(
            "fcmToken" to token,
            "updatedAt" to Date()
        )
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { e -> Log.w(TAG, "Error updating token", e) }
    }

    private companion object {
        private const val TAG = "UserProfileRepository"
        private const val COLLECTION_USERS = "users"
        private const val FIELD_NAME = "name"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_FCM_TOKEN = "fcmToken"
        private const val FIELD_UPDATED_AT = "updatedAt"
        private const val FIELD_PROFILES = "profiles"
        private const val FIELD_ACTIVE_EXTERNAL_USER_ID = "activeExternalUserId"
    }
}

private data class FirestoreUserProfileDto(
    val name: String = "",
    val email: String = "",
    val fcmToken: String = "",
    val updatedAt: Date = Date()
)
