package com.example.wish_list.domain.model

data class GiftItem(
    val id: String,
    val wishlistId: String,
    val title: String,
    val description: String?,
    val link: String?,
    val price: Double?,
    val priority: GiftPriority,
    val status: GiftItemStatus
)

enum class GiftItemStatus {
    AVAILABLE,
    RESERVED,
    GIFTED
}

enum class GiftPriority {
    LOW,
    MEDIUM,
    HIGH
}
