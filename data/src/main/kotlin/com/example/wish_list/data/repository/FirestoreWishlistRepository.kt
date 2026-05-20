package com.example.wish_list.data.repository

import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.WishlistRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreWishlistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : WishlistRepository {

    override suspend fun getWishlistsByOwner(ownerUserId: String): List<Wishlist> {
        val snapshots = firestore.collection(COLLECTION_WISHLISTS)
            .whereEqualTo(FIELD_OWNER_USER_ID, ownerUserId)
            .get()
            .await()
        return snapshots.documents.mapNotNull { it.toWishlist() }
    }

    override suspend fun getWishlistById(wishlistId: String): Wishlist? {
        val snapshot = firestore.collection(COLLECTION_WISHLISTS).document(wishlistId).get().await()
        return snapshot.toWishlist()
    }

    override suspend fun getWishlistByShareCode(shareCode: String): Wishlist? {
        val snapshot = firestore.collection(COLLECTION_WISHLISTS)
            .whereEqualTo(FIELD_SHARE_CODE, shareCode.trim())
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
        return snapshot?.toWishlist()
    }

    override suspend fun createWishlist(wishlist: Wishlist): Wishlist {
        val payload = mapOf(
            FIELD_OWNER_USER_ID to wishlist.ownerUserId,
            FIELD_TITLE to wishlist.title,
            FIELD_DESCRIPTION to wishlist.description,
            FIELD_IS_SHARED to wishlist.isShared,
            FIELD_SHARE_CODE to wishlist.shareCode
        )
        val ref = firestore.collection(COLLECTION_WISHLISTS).add(payload).await()
        val created = ref.get().await()
        return requireNotNull(created.toWishlist()) { "Failed to create wishlist" }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toWishlist(): Wishlist? {
        if (!exists()) return null
        val ownerUserId = getString(FIELD_OWNER_USER_ID) ?: return null
        val title = getString(FIELD_TITLE) ?: return null
        val shareCode = getString(FIELD_SHARE_CODE) ?: return null
        return Wishlist(
            id = id,
            ownerUserId = ownerUserId,
            title = title,
            description = getString(FIELD_DESCRIPTION),
            isShared = getBoolean(FIELD_IS_SHARED) ?: false,
            shareCode = shareCode
        )
    }

    private companion object {
        private const val COLLECTION_WISHLISTS = "wishlists"
        private const val FIELD_OWNER_USER_ID = "ownerUserId"
        private const val FIELD_TITLE = "title"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_IS_SHARED = "isShared"
        private const val FIELD_SHARE_CODE = "shareCode"
    }
}
