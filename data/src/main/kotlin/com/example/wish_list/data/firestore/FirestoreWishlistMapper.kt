package com.example.wish_list.data.firestore

import com.example.wish_list.domain.model.Wishlist
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.toWishlistOrNull(): Wishlist? {
    val payload = toObject(FirestoreWishlistDocument::class.java) ?: return null
    if (payload.ownerUserId.isBlank() || payload.title.isBlank() || payload.shareCode.isBlank()) return null
    return Wishlist(
        id = id,
        ownerUserId = payload.ownerUserId,
        title = payload.title,
        description = payload.description,
        isShared = payload.isShared,
        shareCode = payload.shareCode
    )
}
