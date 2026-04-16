package com.example.wish_list.data.mapper

import com.example.wish_list.data.entity.WishlistEntity
import com.example.wish_list.domain.model.Wishlist

fun WishlistEntity.toDomain(): Wishlist = Wishlist(
    id = id,
    ownerUserId = ownerUserId,
    title = title,
    description = description,
    isShared = isShared,
    shareCode = shareCode
)

fun Wishlist.toEntity(): WishlistEntity = WishlistEntity(
    id = id,
    ownerUserId = ownerUserId,
    title = title,
    description = description,
    isShared = isShared,
    shareCode = shareCode
)
