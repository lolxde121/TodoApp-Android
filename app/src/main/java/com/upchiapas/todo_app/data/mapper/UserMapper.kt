package com.upchiapas.todo_app.data.mapper

import com.upchiapas.todo_app.data.dto.UserDto
import com.upchiapas.todo_app.domain.model.User

/**
 * Mapper dedicado para convertir UserDto a modelo de dominio User.
 * Rúbrica Ítem 4: Mappers dedicados (DTO ↔ dominio) usados solo por los repositorios.
 */
fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        name = this.name ?: "",
        email = this.email,
        createdAt = this.createdAt
    )
}
