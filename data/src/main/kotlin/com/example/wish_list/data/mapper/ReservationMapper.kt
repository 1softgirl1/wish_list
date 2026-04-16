package com.example.wish_list.data.mapper

import com.example.wish_list.data.entity.ReservationEntity
import com.example.wish_list.domain.model.Reservation
import com.example.wish_list.domain.model.ReservationStatus

fun ReservationEntity.toDomain(): Reservation = Reservation(
    id = id,
    giftItemId = giftItemId,
    reservedByUserId = reservedByUserId,
    status = ReservationStatus.valueOf(status)
)

fun Reservation.toEntity(): ReservationEntity = ReservationEntity(
    id = id,
    giftItemId = giftItemId,
    reservedByUserId = reservedByUserId,
    status = status.name
)
