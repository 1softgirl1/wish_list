package com.example.wish_list.domain.model

data class Reservation(
    val id: String,
    val giftItemId: String,
    val reservedByUserId: String,
    val status: ReservationStatus
)

enum class ReservationStatus {
    ACTIVE,
    CANCELLED,
    COMPLETED
}
