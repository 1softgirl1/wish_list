package com.example.wish_list.domain.repository

import com.example.wish_list.domain.model.Wishlist
import kotlinx.coroutines.flow.Flow

interface RealtimeWishlistRepository {
    fun observeWishlistsByOwner(ownerUserId: String): Flow<List<Wishlist>>
}
