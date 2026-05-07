package com.example.wish_list.auth

enum class AuthProvider {
    YANDEX,
    VK
}

data class AuthSession(
    val token: String,
    val userName: String,
    val userId: String,
    val email: String?,
    val provider: AuthProvider,
    val expiresAtMillis: Long
) {
    fun isValid(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return token.isNotBlank() && expiresAtMillis > currentTimeMillis
    }
}

sealed class AuthResult {
    data class Success(val session: AuthSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object Cancelled : AuthResult()
}
