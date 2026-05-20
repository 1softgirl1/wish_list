package com.example.wish_list.data.repository

import com.example.wish_list.data.entity.UserEntity
import com.example.wish_list.data.local.DemoLocalStore
import com.example.wish_list.data.mapper.toDomain
import com.example.wish_list.domain.model.User
import com.example.wish_list.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalAwareUserRepository @Inject constructor(
    private val store: DemoLocalStore,
    private val firestore: FirebaseFirestore
) : UserRepository {
    override suspend fun getCurrentUser(): User? {
        return store.users.firstOrNull { it.id == store.currentUserId }?.toDomain()
    }

    override suspend fun getUserById(userId: String): User? {
        val local = store.users.firstOrNull { it.id == userId }?.toDomain()
        if (local != null) return local

        val snapshot = firestore.collection(COLLECTION_USERS)
            .whereEqualTo(FIELD_ACTIVE_EXTERNAL_USER_ID, userId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: return null

        @Suppress("UNCHECKED_CAST")
        val profiles = snapshot.get(FIELD_PROFILES) as? Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val profile = profiles?.get(userId) as? Map<String, Any?>
        val name = (profile?.get("name") as? String)
            ?.takeIf { it.isNotBlank() }
            ?: snapshot.getString(FIELD_NAME).orEmpty().ifBlank { "User" }

        val user = User(id = userId, name = name)
        store.users += UserEntity(id = user.id, name = user.name)
        return user
    }

    override suspend fun getAllUsers(): List<User> = store.users.map { it.toDomain() }

    override suspend fun switchCurrentUser(userId: String) {
        if (store.users.any { it.id == userId }) {
            store.currentUserId = userId
        }
    }

    override suspend fun ensureCurrentUser(userId: String, userName: String) {
        if (store.users.none { it.id == userId }) {
            store.users += UserEntity(
                id = userId,
                name = userName.ifBlank { "User" }
            )
        }
        store.currentUserId = userId
    }

    private companion object {
        private const val COLLECTION_USERS = "users"
        private const val FIELD_NAME = "name"
        private const val FIELD_PROFILES = "profiles"
        private const val FIELD_ACTIVE_EXTERNAL_USER_ID = "activeExternalUserId"
    }
}
