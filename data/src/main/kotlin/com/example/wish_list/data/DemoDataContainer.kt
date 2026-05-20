package com.example.wish_list.data

import com.example.wish_list.data.local.DemoLocalStore
import com.example.wish_list.data.repository.InMemoryGiftItemRepository
import com.example.wish_list.data.repository.InMemoryReservationRepository
import com.example.wish_list.data.repository.InMemoryUserRepository
import com.example.wish_list.data.repository.InMemoryWishlistRepository
import com.example.wish_list.domain.repository.GiftItemRepository
import com.example.wish_list.domain.repository.RealtimeWishlistRepository
import com.example.wish_list.domain.repository.ReservationRepository
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository

class DemoDataContainer(
    val userRepository: UserRepository,
    val wishlistRepository: WishlistRepository,
    val giftItemRepository: GiftItemRepository,
    val reservationRepository: ReservationRepository,
    val realtimeWishlistRepository: RealtimeWishlistRepository? = null
) {
    constructor() : this(
        userRepository = InMemoryUserRepository(defaultStore),
        wishlistRepository = InMemoryWishlistRepository(defaultStore),
        giftItemRepository = InMemoryGiftItemRepository(defaultStore),
        reservationRepository = InMemoryReservationRepository(defaultStore),
        realtimeWishlistRepository = null
    )

    private companion object {
        val defaultStore = DemoLocalStore()
    }
}
