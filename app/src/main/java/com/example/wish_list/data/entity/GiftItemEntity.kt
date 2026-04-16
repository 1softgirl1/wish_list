package com.example.wish_list.data.entity

data class GiftItemEntity(
    val id: String,
    val wishlistId: String,
    val title: String,
    val description: String?,
    val link: String?,
    val price: Double?,
    val priority: String,
    val status: String
)
