package com.example.wish_list.di

import com.example.wish_list.AppMetricaAnalyticsService
import com.example.wish_list.auth.AuthService
import com.example.wish_list.auth.ExternalAuthService
import com.example.wish_list.data.repository.FirestoreGiftItemRepository
import com.example.wish_list.data.repository.FirestoreReservationRepository
import com.example.wish_list.data.repository.FirestoreRealtimeWishlistRepository
import com.example.wish_list.data.repository.FirestoreWishlistRepository
import com.example.wish_list.data.repository.ExternalAwareUserRepository
import com.example.wish_list.domain.repository.GiftItemRepository
import com.example.wish_list.domain.repository.RealtimeWishlistRepository
import com.example.wish_list.domain.repository.ReservationRepository
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository
import com.example.wish_list.firebase.FirebaseRemoteConfigService
import com.example.wish_list.firebase.RemoteConfigService
import com.example.wish_list.ui.analytics.AnalyticsService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthService(impl: ExternalAuthService): AuthService

    @Binds
    @Singleton
    abstract fun bindAnalyticsService(impl: AppMetricaAnalyticsService): AnalyticsService

    @Binds
    @Singleton
    abstract fun bindRemoteConfigService(impl: FirebaseRemoteConfigService): RemoteConfigService

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: ExternalAwareUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(impl: FirestoreWishlistRepository): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindGiftItemRepository(impl: FirestoreGiftItemRepository): GiftItemRepository

    @Binds
    @Singleton
    abstract fun bindReservationRepository(impl: FirestoreReservationRepository): ReservationRepository

    @Binds
    @Singleton
    abstract fun bindRealtimeWishlistRepository(
        impl: FirestoreRealtimeWishlistRepository
    ): RealtimeWishlistRepository
}
