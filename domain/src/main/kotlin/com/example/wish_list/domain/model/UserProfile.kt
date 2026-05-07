package com.example.wish_list.domain.model

data class UserProfile(
    val userId: String,
    val name: String,
    val email: String,
    val fcmToken: String,
    val updatedAtMillis: Long
)
