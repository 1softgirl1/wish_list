package com.example.wish_list.data.mapper

import com.example.wish_list.data.entity.GiftItemEntity
import com.example.wish_list.domain.model.GiftItem
import com.example.wish_list.domain.model.GiftItemStatus
import com.example.wish_list.domain.model.GiftPriority

fun GiftItemEntity.toDomain(): GiftItem = GiftItem(
    id = id,
    wishlistId = wishlistId,
    title = title,
    description = description,
    link = link,
    price = price,
    priority = GiftPriority.valueOf(priority),
    status = GiftItemStatus.valueOf(status)
)

fun GiftItem.toEntity(): GiftItemEntity = GiftItemEntity(
    id = id,
    wishlistId = wishlistId,
    title = title,
    description = description,
    link = link,
    price = price,
    priority = priority.name,
    status = status.name
)
