package com.example.wish_list.data.repository

import com.example.wish_list.data.local.DemoLocalStore
import com.example.wish_list.data.mapper.toDomain
import com.example.wish_list.data.mapper.toEntity
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.repository.GiftItemRepository
import javax.inject.Inject

class InMemoryGiftItemRepository @Inject constructor(
    private val store: DemoLocalStore
) : GiftItemRepository {
    override suspend fun getGiftItemById(giftItemId: String): GiftItem? {
        return store.giftItems.firstOrNull { it.id == giftItemId }?.toDomain()
    }

    override suspend fun getGiftItemsByWishlistId(wishlistId: String): List<GiftItem> {
        return store.giftItems
            .filter { it.wishlistId == wishlistId }
            .map { it.toDomain() }
    }

    override suspend fun addGiftItem(giftItem: GiftItem): GiftItem {
        val entity = giftItem.copy(id = store.nextGiftId()).toEntity()
        store.giftItems += entity
        return entity.toDomain()
    }

    override suspend fun updateGiftItem(giftItem: GiftItem): GiftItem {
        val index = store.giftItems.indexOfFirst { it.id == giftItem.id }
        require(index >= 0) { "Gift item not found: ${giftItem.id}" }

        val entity = giftItem.toEntity()
        store.giftItems[index] = entity
        return entity.toDomain()
    }
}
