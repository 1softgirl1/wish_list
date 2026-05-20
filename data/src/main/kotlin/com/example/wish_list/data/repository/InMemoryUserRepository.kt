package com.example.wish_list.data.repository

import com.example.wish_list.data.local.DemoLocalStore
import com.example.wish_list.data.mapper.toDomain
import com.example.wish_list.data.entity.UserEntity
import com.example.wish_list.domain.model.User
import com.example.wish_list.domain.repository.UserRepository
import javax.inject.Inject

class InMemoryUserRepository @Inject constructor(
    private val store: DemoLocalStore
) : UserRepository {
    override suspend fun getCurrentUser(): User? {
        return store.users.firstOrNull { it.id == store.currentUserId }?.toDomain()
    }

    override suspend fun getUserById(userId: String): User? {
        return store.users.firstOrNull { it.id == userId }?.toDomain()
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
}
