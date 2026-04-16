package com.example.wish_list.data.entity

data class WishlistEntity(
    val id: String,
    val ownerUserId: String,
    val title: String,
    val description: String?,
    val isShared: Boolean,
    val shareCode: String
)
