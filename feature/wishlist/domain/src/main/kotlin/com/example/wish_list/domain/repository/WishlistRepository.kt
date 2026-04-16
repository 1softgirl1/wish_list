package com.example.wish_list.domain.repository

import com.example.wish_list.domain.model.Wishlist

interface WishlistRepository {
    suspend fun getWishlistsByOwner(ownerUserId: String): List<Wishlist>
    suspend fun getWishlistById(wishlistId: String): Wishlist?
    suspend fun getWishlistByShareCode(shareCode: String): Wishlist?
    suspend fun createWishlist(wishlist: Wishlist): Wishlist
}
