package com.example.wish_list.data.repository

import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.model.GiftItemStatus
import com.example.wish_list.domain.model.GiftPriority
import com.example.wish_list.domain.repository.GiftItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreGiftItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : GiftItemRepository {
    override suspend fun getGiftItemById(giftItemId: String): GiftItem? {
        val snapshot = firestore.collection(COLLECTION_GIFTS).document(giftItemId).get().await()
        return snapshot.toGiftItem()
    }

    override suspend fun getGiftItemsByWishlistId(wishlistId: String): List<GiftItem> {
        val snapshots = firestore.collection(COLLECTION_GIFTS)
            .whereEqualTo(FIELD_WISHLIST_ID, wishlistId)
            .get()
            .await()
        return snapshots.documents.mapNotNull { it.toGiftItem() }
    }

    override suspend fun addGiftItem(giftItem: GiftItem): GiftItem {
        val payload = mapOf(
            FIELD_WISHLIST_ID to giftItem.wishlistId,
            FIELD_TITLE to giftItem.title,
            FIELD_DESCRIPTION to giftItem.description,
            FIELD_LINK to giftItem.link,
            FIELD_PRICE to giftItem.price,
            FIELD_PRIORITY to giftItem.priority.name,
            FIELD_STATUS to giftItem.status.name
        )
        val ref = firestore.collection(COLLECTION_GIFTS).add(payload).await()
        val created = ref.get().await()
        return requireNotNull(created.toGiftItem()) { "Failed to create gift item" }
    }

    override suspend fun updateGiftItem(giftItem: GiftItem): GiftItem {
        val payload = mapOf(
            FIELD_WISHLIST_ID to giftItem.wishlistId,
            FIELD_TITLE to giftItem.title,
            FIELD_DESCRIPTION to giftItem.description,
            FIELD_LINK to giftItem.link,
            FIELD_PRICE to giftItem.price,
            FIELD_PRIORITY to giftItem.priority.name,
            FIELD_STATUS to giftItem.status.name
        )
        firestore.collection(COLLECTION_GIFTS).document(giftItem.id).set(payload).await()
        return requireNotNull(getGiftItemById(giftItem.id)) { "Gift item not found: ${giftItem.id}" }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toGiftItem(): GiftItem? {
        if (!exists()) return null
        val wishlistId = getString(FIELD_WISHLIST_ID) ?: return null
        val title = getString(FIELD_TITLE) ?: return null
        val priorityName = getString(FIELD_PRIORITY) ?: GiftPriority.MEDIUM.name
        val statusName = getString(FIELD_STATUS) ?: GiftItemStatus.AVAILABLE.name
        return GiftItem(
            id = id,
            wishlistId = wishlistId,
            title = title,
            description = getString(FIELD_DESCRIPTION),
            link = getString(FIELD_LINK),
            price = getDouble(FIELD_PRICE),
            priority = runCatching { GiftPriority.valueOf(priorityName) }.getOrDefault(GiftPriority.MEDIUM),
            status = runCatching { GiftItemStatus.valueOf(statusName) }.getOrDefault(GiftItemStatus.AVAILABLE)
        )
    }

    private companion object {
        private const val COLLECTION_GIFTS = "giftItems"
        private const val FIELD_WISHLIST_ID = "wishlistId"
        private const val FIELD_TITLE = "title"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_LINK = "link"
        private const val FIELD_PRICE = "price"
        private const val FIELD_PRIORITY = "priority"
        private const val FIELD_STATUS = "status"
    }
}
