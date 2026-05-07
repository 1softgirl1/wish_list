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

class UserProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val registration = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed", error)
                    // Firestore permission/network errors should not crash UI observers.
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val dto = snapshot.toObject(FirestoreUserProfileDto::class.java)
                val profile = UserProfile(
                    userId = userId,
                    name = dto?.name.orEmpty(),
                    email = dto?.email.orEmpty(),
                    fcmToken = dto?.fcmToken.orEmpty(),
                    updatedAtMillis = dto?.updatedAt?.time ?: 0L
                )
                trySend(profile)
            }
        awaitClose { registration.remove() }
    }

    fun saveOrUpdateProfile(session: AuthSession, fcmToken: String?) {
        val profile = FirestoreUserProfileDto(
            name = session.userName,
            email = session.email.orEmpty(),
            fcmToken = fcmToken.orEmpty(),
            updatedAt = Date()
        )
        firestore.collection(COLLECTION_USERS)
            .document(session.userId)
            .set(profile)
            .addOnSuccessListener { Log.d(TAG, "Profile saved for ${session.userId}") }
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
    }
}

private data class FirestoreUserProfileDto(
    val name: String = "",
    val email: String = "",
    val fcmToken: String = "",
    val updatedAt: Date = Date()
)
