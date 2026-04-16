package com.example.wish_list.domain.usecase.reservation

import com.example.wish_list.domain.exception.NotFoundException
import com.example.wish_list.domain.model.Reservation
import com.example.wish_list.domain.repository.ReservationRepository
import com.example.wish_list.domain.repository.UserRepository

class GetMyReservationsUseCase(
    private val userRepository: UserRepository,
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(): List<Reservation> {
        val currentUser = userRepository.getCurrentUser()
            ?: throw NotFoundException("Current user was not found")

        return reservationRepository.getReservationsByUserId(currentUser.id)
    }
}
