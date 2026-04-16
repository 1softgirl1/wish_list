package com.example.wish_list.domain.usecase.wishlist

import com.example.wish_list.domain.exception.AccessDeniedException
import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.WishlistRepository

class GetWishlistByShareCodeUseCase(
    private val wishlistRepository: WishlistRepository
) {
    suspend operator fun invoke(shareCode: String): Wishlist {
        val wishlist = wishlistRepository.getWishlistByShareCode(shareCode.trim())
            ?: throw NotFoundException("Wishlist was not found")

        if (!wishlist.isShared) {
            throw AccessDeniedException("Wishlist is not available by share link")
        }

        return wishlist
    }
}
