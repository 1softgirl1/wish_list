package com.example.wish_list.data.repository

import com.example.wish_list.domain.model.Reservation
import com.example.wish_list.domain.model.ReservationStatus
import com.example.wish_list.domain.repository.ReservationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreReservationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReservationRepository {
    override suspend fun getReservationById(reservationId: String): Reservation? {
        val snapshot = firestore.collection(COLLECTION_RESERVATIONS).document(reservationId).get().await()
        return snapshot.toReservation()
    }

    override suspend fun getActiveReservationForGiftItem(giftItemId: String): Reservation? {
        val snapshot = firestore.collection(COLLECTION_RESERVATIONS)
            .whereEqualTo(FIELD_GIFT_ITEM_ID, giftItemId)
            .whereEqualTo(FIELD_STATUS, ReservationStatus.ACTIVE.name)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
        return snapshot?.toReservation()
    }

    override suspend fun getReservationsByUserId(userId: String): List<Reservation> {
        val snapshots = firestore.collection(COLLECTION_RESERVATIONS)
            .whereEqualTo(FIELD_RESERVED_BY_USER_ID, userId)
            .get()
            .await()
        return snapshots.documents.mapNotNull { it.toReservation() }
    }

    override suspend fun createReservation(reservation: Reservation): Reservation {
        val payload = mapOf(
            FIELD_GIFT_ITEM_ID to reservation.giftItemId,
            FIELD_RESERVED_BY_USER_ID to reservation.reservedByUserId,
            FIELD_STATUS to reservation.status.name
        )
        val ref = firestore.collection(COLLECTION_RESERVATIONS).add(payload).await()
        val created = ref.get().await()
        return requireNotNull(created.toReservation()) { "Failed to create reservation" }
    }

    override suspend fun updateReservation(reservation: Reservation): Reservation {
        val payload = mapOf(
            FIELD_GIFT_ITEM_ID to reservation.giftItemId,
            FIELD_RESERVED_BY_USER_ID to reservation.reservedByUserId,
            FIELD_STATUS to reservation.status.name
        )
        firestore.collection(COLLECTION_RESERVATIONS).document(reservation.id).set(payload).await()
        return requireNotNull(getReservationById(reservation.id)) { "Reservation not found: ${reservation.id}" }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReservation(): Reservation? {
        if (!exists()) return null
        val giftItemId = getString(FIELD_GIFT_ITEM_ID) ?: return null
        val reservedByUserId = getString(FIELD_RESERVED_BY_USER_ID) ?: return null
        val statusName = getString(FIELD_STATUS) ?: ReservationStatus.ACTIVE.name
        return Reservation(
            id = id,
            giftItemId = giftItemId,
            reservedByUserId = reservedByUserId,
            status = runCatching { ReservationStatus.valueOf(statusName) }.getOrDefault(ReservationStatus.ACTIVE)
        )
    }

    private companion object {
        private const val COLLECTION_RESERVATIONS = "reservations"
        private const val FIELD_GIFT_ITEM_ID = "giftItemId"
        private const val FIELD_RESERVED_BY_USER_ID = "reservedByUserId"
        private const val FIELD_STATUS = "status"
    }
}
