package com.example.wish_list.domain.usecase.gift

import com.example.wish_list.domain.exception.AccessDeniedException
import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.repository.GiftItemRepository
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository

class GetGiftItemsForWishlistUseCase(
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
    private val giftItemRepository: GiftItemRepository
) {
    suspend operator fun invoke(wishlistId: String): List<GiftItem> {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")
        val wishlist = wishlistRepository.getWishlistById(wishlistId)
            ?: throw NotFoundException("Wishlist was not found")

        if (wishlist.ownerUserId != currentUser.id) {
            throw AccessDeniedException("Only the owner can view all wishlist gift items")
        }

        return giftItemRepository.getGiftItemsByWishlistId(wishlistId)
    }
}
