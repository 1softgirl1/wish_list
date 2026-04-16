package com.example.wish_list.domain.usecase.reservation

import com.example.wish_list.domain.exception.AccessDeniedException
import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.exception.ValidationException
import com.example.wish_list.domain.model.GiftItemStatus
import com.example.wish_list.domain.model.Reservation
import com.example.wish_list.domain.model.ReservationStatus
import com.example.wish_list.domain.repository.GiftItemRepository
import com.example.wish_list.domain.repository.ReservationRepository
import com.example.wish_list.domain.repository.UserRepository

class MarkGiftAsGiftedUseCase(
    private val userRepository: UserRepository,
    private val giftItemRepository: GiftItemRepository,
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String): Reservation {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")
        val reservation = reservationRepository.getReservationById(reservationId)
            ?: throw NotFoundException("Reservation was not found")

        if (reservation.reservedByUserId != currentUser.id) {
            throw AccessDeniedException("Only the reservation owner can mark gift as gifted")
        }
        if (reservation.status != ReservationStatus.ACTIVE) {
            throw ValidationException("Only active reservations can be marked as gifted")
        }

        val giftItem = giftItemRepository.getGiftItemById(reservation.giftItemId)
            ?: throw NotFoundException("Gift item was not found")
        if (giftItem.status != GiftItemStatus.RESERVED) {
            throw ValidationException("Only reserved gift items can be marked as gifted")
        }

        giftItemRepository.updateGiftItem(giftItem.copy(status = GiftItemStatus.GIFTED))

        val updatedReservation = reservation.copy(status = ReservationStatus.COMPLETED)
        return reservationRepository.updateReservation(updatedReservation)
    }
}
