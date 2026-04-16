package com.example.wish_list.di

import com.example.wish_list.data.DemoDataContainer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WishlistDataModule {
    @Provides
    @Singleton
    fun provideDemoDataContainer(): DemoDataContainer = DemoDataContainer()
}
