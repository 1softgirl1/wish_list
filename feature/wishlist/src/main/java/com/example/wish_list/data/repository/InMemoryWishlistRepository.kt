package com.example.wish_list.data.repository

import com.example.wish_list.data.local.DemoLocalStore
import com.example.wish_list.data.mapper.toDomain
import com.example.wish_list.data.mapper.toEntity
import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.WishlistRepository

class InMemoryWishlistRepository(
    private val store: DemoLocalStore
) : WishlistRepository {
    override suspend fun getWishlistsByOwner(ownerUserId: String): List<Wishlist> {
        return store.wishlists
            .filter { it.ownerUserId == ownerUserId }
            .map { it.toDomain() }
    }

    override suspend fun getWishlistById(wishlistId: String): Wishlist? {
        return store.wishlists.firstOrNull { it.id == wishlistId }?.toDomain()
    }

    override suspend fun getWishlistByShareCode(shareCode: String): Wishlist? {
        return store.wishlists.firstOrNull { it.shareCode.equals(shareCode, ignoreCase = true) }?.toDomain()
    }

    override suspend fun createWishlist(wishlist: Wishlist): Wishlist {
        val entity = wishlist.copy(id = store.nextWishlistId()).toEntity()
        store.wishlists += entity
        return entity.toDomain()
    }
}
