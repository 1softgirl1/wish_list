package com.example.wish_list.domain.usecase.wishlist

import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.exception.ValidationException
import com.example.wish_list.domain.model.Wishlist
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository

class CreateWishlistUseCase(
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        isShared: Boolean,
        shareCode: String
    ): Wishlist {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")

        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) {
            throw ValidationException("Wishlist title must not be blank")
        }

        val normalizedShareCode = shareCode.trim()
        if (normalizedShareCode.isEmpty()) {
            throw ValidationException("Share code must not be blank")
        }

        return wishlistRepository.createWishlist(
            Wishlist(
                id = "",
                ownerUserId = currentUser.id,
                title = normalizedTitle,
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                isShared = isShared,
                shareCode = normalizedShareCode
            )
        )
    }
}
