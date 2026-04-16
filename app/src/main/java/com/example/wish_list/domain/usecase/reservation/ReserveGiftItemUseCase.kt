package com.example.wish_list.domain.usecase.reservation

import com.example.wish_list.domain.exception.AccessDeniedException
import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.exception.ValidationException
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.model.GiftItemStatus
import com.example.wish_list.domain.model.Reservation
import com.example.wish_list.domain.model.ReservationStatus
import com.example.wish_list.domain.repository.GiftItemRepository
import com.example.wish_list.domain.repository.ReservationRepository
import com.example.wish_list.domain.repository.UserRepository
import com.example.wish_list.domain.repository.WishlistRepository

class ReserveGiftItemUseCase(
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
    private val giftItemRepository: GiftItemRepository,
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(giftItemId: String): Reservation {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")
        val giftItem = giftItemRepository.getGiftItemById(giftItemId)
            ?: throw NotFoundException("Gift item was not found")
        val wishlist = wishlistRepository.getWishlistById(giftItem.wishlistId)
            ?: throw NotFoundException("Wishlist was not found")

        if (!wishlist.isShared) {
            throw AccessDeniedException("Gift item cannot be reserved from a private wishlist")
        }
        if (wishlist.ownerUserId == currentUser.id) {
            throw AccessDeniedException("Owner cannot reserve a gift item from their own wishlist")
        }
        if (giftItem.status != GiftItemStatus.AVAILABLE) {
            throw ValidationException("Only available gift items can be reserved")
        }
        if (reservationRepository.getActiveReservationForGiftItem(giftItem.id) != null) {
            throw ValidationException("Gift item is already reserved")
        }

        val updatedGiftItem = giftItem.copy(status = GiftItemStatus.RESERVED)
        giftItemRepository.updateGiftItem(updatedGiftItem)

        return reservationRepository.createReservation(
            Reservation(
                id = "",
                giftItemId = giftItem.id,
                reservedByUserId = currentUser.id,
                status = ReservationStatus.ACTIVE
            )
        )
    }
}
