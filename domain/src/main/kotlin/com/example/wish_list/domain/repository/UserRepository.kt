package com.example.wish_list.domain.repository

import com.example.wish_list.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun getUserById(userId: String): User?
    suspend fun getAllUsers(): List<User>
    suspend fun switchCurrentUser(userId: String)
    suspend fun ensureCurrentUser(userId: String, userName: String)
}
