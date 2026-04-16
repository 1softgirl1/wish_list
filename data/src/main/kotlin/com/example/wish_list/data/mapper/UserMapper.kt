package com.example.wish_list.data.mapper

import com.example.wish_list.data.entity.UserEntity
import com.example.wish_list.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name
)
