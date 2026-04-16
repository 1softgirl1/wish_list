package com.example.wish_list.feature.reservation

object ReservationStatusFormatter {
    fun toUi(status: String): String = status.lowercase().replaceFirstChar { it.uppercase() }
}
