package com.example.wish_list.data.firestore

data class FirestoreWishlistDocument(
    val ownerUserId: String = "",
    val title: String = "",
    val description: String? = null,
    val isShared: Boolean = false,
    val shareCode: String = ""
)
