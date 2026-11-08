package com.upchiapas.todo_app.domain.repository

import com.upchiapas.todo_app.domain.model.User

/**
 * Contrato de repositorio de Autenticación en la capa de Domain.
 * Rúbrica Ítem 4: Las interfaces de repositorio en domain exponen solo modelos de dominio.
 */
interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun login(email: String, password: String): Result<String>
    fun logout()
    fun hasActiveSession(): Boolean
}
