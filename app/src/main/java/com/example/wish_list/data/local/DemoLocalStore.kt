package com.example.wish_list.data.local

import com.example.wish_list.data.entity.GiftItemEntity
import com.example.wish_list.data.entity.ReservationEntity
import com.example.wish_list.data.entity.UserEntity
import com.example.wish_list.data.entity.WishlistEntity

class DemoLocalStore {
    private var userCounter = 3
    private var wishlistCounter = 3
    private var giftCounter = 5
    private var reservationCounter = 1

    val users = mutableListOf(
        UserEntity(id = "user_1", name = "Alice"),
        UserEntity(id = "user_2", name = "Bob")
    )

    val wishlists = mutableListOf(
        WishlistEntity(
            id = "wishlist_1",
            ownerUserId = "user_1",
            title = "Birthday 2026",
            description = "Things I would really like to get",
            isShared = true,
            shareCode = "ALICE2026"
        ),
        WishlistEntity(
            id = "wishlist_2",
            ownerUserId = "user_2",
            title = "My Tech Wishlist",
            description = "Gear I am planning to buy or receive",
            isShared = true,
            shareCode = "BOBTECH"
        )
    )

    val giftItems = mutableListOf(
        GiftItemEntity(
            id = "gift_1",
            wishlistId = "wishlist_1",
            title = "Sony WH-1000XM5",
            description = "Wireless noise cancelling headphones",
            link = "https://example.com/headphones",
            price = 329.99,
            priority = "HIGH",
            status = "AVAILABLE"
        ),
        GiftItemEntity(
            id = "gift_2",
            wishlistId = "wishlist_1",
            title = "Mechanical keyboard",
            description = "75% layout, tactile switches",
            link = "https://example.com/keyboard",
            price = 149.99,
            priority = "MEDIUM",
            status = "AVAILABLE"
        ),
        GiftItemEntity(
            id = "gift_3",
            wishlistId = "wishlist_1",
            title = "Gift card",
            description = "Useful for flexible gifts",
            link = null,
            price = 50.0,
            priority = "LOW",
            status = "AVAILABLE"
        ),
        GiftItemEntity(
            id = "gift_4",
            wishlistId = "wishlist_2",
            title = "Kindle Paperwhite",
            description = "For reading on trips",
            link = "https://example.com/kindle",
            price = 179.99,
            priority = "HIGH",
            status = "AVAILABLE"
        )
    )

    val reservations = mutableListOf<ReservationEntity>()

    var currentUserId: String = "user_1"

    fun nextWishlistId(): String = "wishlist_${wishlistCounter++}"

    fun nextGiftId(): String = "gift_${giftCounter++}"

    fun nextReservationId(): String = "reservation_${reservationCounter++}"
}
