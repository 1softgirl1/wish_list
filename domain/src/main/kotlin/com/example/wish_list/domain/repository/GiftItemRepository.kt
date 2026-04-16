package com.example.wish_list.domain.repository

import com.example.wish_list.domain.model.GiftItem

interface GiftItemRepository {
    suspend fun getGiftItemById(giftItemId: String): GiftItem?
    suspend fun getGiftItemsByWishlistId(wishlistId: String): List<GiftItem>
    suspend fun addGiftItem(giftItem: GiftItem): GiftItem
    suspend fun updateGiftItem(giftItem: GiftItem): GiftItem
}
