package com.example.wish_list.data.entity

data class ReservationEntity(
    val id: String,
    val giftItemId: String,
    val reservedByUserId: String,
    val status: String
)
