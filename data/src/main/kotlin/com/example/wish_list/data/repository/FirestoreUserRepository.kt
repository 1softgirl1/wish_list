package com.example.wish_list.data.repository

import com.example.wish_list.domain.model.User
import com.example.wish_list.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    private var currentUserId: String? = null

    override suspend fun getCurrentUser(): User? {
        val id = currentUserId ?: return null
        return getUserById(id)
    }

    override suspend fun getUserById(userId: String): User? {
        val snapshot = firestore.collection(COLLECTION_USERS).document(userId).get().await()
        if (!snapshot.exists()) return null
        val name = snapshot.getString(FIELD_NAME).orEmpty().ifBlank { "User" }
        return User(id = userId, name = name)
    }

    override suspend fun getAllUsers(): List<User> {
        // Security rules allow access to users/{request.auth.uid}, not full collection scans.
        // Returning only current user keeps app logic consistent and avoids PERMISSION_DENIED.
        return listOfNotNull(getCurrentUser())
    }

    override suspend fun switchCurrentUser(userId: String) {
        val userExists = firestore.collection(COLLECTION_USERS).document(userId).get().await().exists()
        if (userExists) {
            currentUserId = userId
        }
    }

    override suspend fun ensureCurrentUser(userId: String, userName: String) {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .set(
                mapOf(
                    FIELD_NAME to userName.ifBlank { "User" }
                )
            )
            .await()
        currentUserId = userId
    }

    private companion object {
        private const val COLLECTION_USERS = "users"
        private const val FIELD_NAME = "name"
    }
}
