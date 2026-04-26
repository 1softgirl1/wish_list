package com.example.wish_list.auth

import androidx.activity.ComponentActivity

interface AuthService {
    suspend fun loginWithYandex(activity: ComponentActivity?): AuthResult
    suspend fun loginWithVk(activity: ComponentActivity?): AuthResult
    fun getSavedSession(): AuthSession?
    fun saveSession(session: AuthSession)
    fun clearSession()
}
