package com.example.wish_list.data

import com.example.wish_list.data.local.DemoLocalStore
import com.example.wish_list.data.repository.FirestoreRealtimeWishlistRepository
import com.example.wish_list.data.repository.InMemoryGiftItemRepository
import com.example.wish_list.data.repository.InMemoryReservationRepository
import com.example.wish_list.data.repository.InMemoryUserRepository
import com.example.wish_list.data.repository.InMemoryWishlistRepository
import com.example.wish_list.domain.repository.RealtimeWishlistRepository

class DemoDataContainer {
    private val store = DemoLocalStore()

    val userRepository = InMemoryUserRepository(store)
    val wishlistRepository = InMemoryWishlistRepository(store)
    val giftItemRepository = InMemoryGiftItemRepository(store)
    val reservationRepository = InMemoryReservationRepository(store)
    val realtimeWishlistRepository: RealtimeWishlistRepository? = runCatching {
        FirestoreRealtimeWishlistRepository()
    }.getOrNull()
}
