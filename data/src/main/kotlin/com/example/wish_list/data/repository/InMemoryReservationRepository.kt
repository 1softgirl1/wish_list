package com.example.wish_list.data.repository

import com.example.wish_list.data.local.DemoLocalStore
import com.example.wish_list.data.mapper.toDomain
import com.example.wish_list.data.mapper.toEntity
import com.example.wish_list.domain.model.Reservation
import com.example.wish_list.domain.model.ReservationStatus
import com.example.wish_list.domain.repository.ReservationRepository
import javax.inject.Inject

class InMemoryReservationRepository @Inject constructor(
    private val store: DemoLocalStore
) : ReservationRepository {
    override suspend fun getReservationById(reservationId: String): Reservation? {
        return store.reservations.firstOrNull { it.id == reservationId }?.toDomain()
    }

    override suspend fun getActiveReservationForGiftItem(giftItemId: String): Reservation? {
        return store.reservations
            .firstOrNull { it.giftItemId == giftItemId && it.status == ReservationStatus.ACTIVE.name }
            ?.toDomain()
    }

    override suspend fun getReservationsByUserId(userId: String): List<Reservation> {
        return store.reservations
            .filter { it.reservedByUserId == userId }
            .map { it.toDomain() }
    }

    override suspend fun createReservation(reservation: Reservation): Reservation {
        val entity = reservation.copy(id = store.nextReservationId()).toEntity()
        store.reservations += entity
        return entity.toDomain()
    }

    override suspend fun updateReservation(reservation: Reservation): Reservation {
        val index = store.reservations.indexOfFirst { it.id == reservation.id }
        require(index >= 0) { "Reservation not found: ${reservation.id}" }

        val entity = reservation.toEntity()
        store.reservations[index] = entity
        return entity.toDomain()
    }
}
