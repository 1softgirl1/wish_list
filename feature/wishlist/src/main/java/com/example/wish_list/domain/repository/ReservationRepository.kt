package com.example.wish_list.domain.repository

import com.example.wish_list.domain.model.Reservation

interface ReservationRepository {
    suspend fun getReservationById(reservationId: String): Reservation?
    suspend fun getActiveReservationForGiftItem(giftItemId: String): Reservation?
    suspend fun getReservationsByUserId(userId: String): List<Reservation>
    suspend fun createReservation(reservation: Reservation): Reservation
    suspend fun updateReservation(reservation: Reservation): Reservation
}
