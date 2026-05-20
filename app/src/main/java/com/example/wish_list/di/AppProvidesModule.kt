package com.example.wish_list.di

import com.example.wish_list.data.DemoDataContainer
import com.example.wish_list.domain.repository.GiftItemRepository
import com.example.wish_list.domain.repository.RealtimeWishlistRepository
import com.example.wish_list.domain.repository.ReservationRepository
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideDemoDataContainer(
        userRepository: UserRepository,
        wishlistRepository: WishlistRepository,
        giftItemRepository: GiftItemRepository,
        reservationRepository: ReservationRepository,
        realtimeWishlistRepository: RealtimeWishlistRepository
    ): DemoDataContainer {
        return DemoDataContainer(
            userRepository = userRepository,
            wishlistRepository = wishlistRepository,
            giftItemRepository = giftItemRepository,
            reservationRepository = reservationRepository,
            realtimeWishlistRepository = realtimeWishlistRepository
        )
    }
}
