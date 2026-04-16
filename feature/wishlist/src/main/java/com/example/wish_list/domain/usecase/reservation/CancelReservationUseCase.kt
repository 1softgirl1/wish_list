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

class CancelReservationUseCase(
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
            throw AccessDeniedException("Only the reservation owner can cancel it")
        }
        if (reservation.status != ReservationStatus.ACTIVE) {
            throw ValidationException("Only active reservations can be cancelled")
        }

        val giftItem = giftItemRepository.getGiftItemById(reservation.giftItemId)
            ?: throw NotFoundException("Gift item was not found")
        giftItemRepository.updateGiftItem(giftItem.copy(status = GiftItemStatus.AVAILABLE))

        val updatedReservation = reservation.copy(status = ReservationStatus.CANCELLED)
        return reservationRepository.updateReservation(updatedReservation)
    }
}
