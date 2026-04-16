package com.example.wish_list.domain.usecase.gift

import com.example.wish_list.domain.exception.AccessDeniedException
import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.exception.ValidationException
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.model.GiftPriority
import com.example.wish_list.domain.repository.GiftItemRepository
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository

class UpdateGiftItemUseCase(
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
    private val giftItemRepository: GiftItemRepository
) {
    suspend operator fun invoke(
        giftItemId: String,
        title: String,
        description: String?,
        link: String?,
        price: Double?,
        priority: GiftPriority
    ): GiftItem {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")
        val giftItem = giftItemRepository.getGiftItemById(giftItemId)
            ?: throw NotFoundException("Gift item was not found")
        val wishlist = wishlistRepository.getWishlistById(giftItem.wishlistId)
            ?: throw NotFoundException("Wishlist was not found")

        if (wishlist.ownerUserId != currentUser.id) {
            throw AccessDeniedException("Only the owner can update gift items")
        }

        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) {
            throw ValidationException("Gift item title must not be blank")
        }
        if (price != null && price < 0.0) {
            throw ValidationException("Gift item price must be greater than or equal to zero")
        }

        return giftItemRepository.updateGiftItem(
            giftItem.copy(
                title = normalizedTitle,
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                link = link?.trim()?.takeIf { it.isNotEmpty() },
                price = price,
                priority = priority
            )
        )
    }
}
