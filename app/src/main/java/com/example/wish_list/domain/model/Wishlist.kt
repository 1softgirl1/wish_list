package com.example.wish_list.domain.model

data class Wishlist(
    val id: String,
    val ownerUserId: String,
    val title: String,
    val description: String?,
    val isShared: Boolean,
    val shareCode: String
)
