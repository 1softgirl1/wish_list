package com.example.wish_list.domain.usecase.wishlist

import com.example.wish_list.domain.exception.AccessDeniedException
import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository

class GetWishlistDetailsUseCase(
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository
) {
    suspend operator fun invoke(wishlistId: String): Wishlist {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")
        val wishlist = wishlistRepository.getWishlistById(wishlistId)
            ?: throw NotFoundException("Wishlist was not found")

        if (wishlist.ownerUserId != currentUser.id) {
            throw AccessDeniedException("Only the owner can view private wishlist details")
        }

        return wishlist
    }
}
