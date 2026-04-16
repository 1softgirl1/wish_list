package com.example.wish_list.domain.usecase.wishlist

import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository

class GetMyWishlistsUseCase(
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository
) {
    suspend operator fun invoke(): List<Wishlist> {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")

        return wishlistRepository.getWishlistsByOwner(currentUser.id)
    }
}
