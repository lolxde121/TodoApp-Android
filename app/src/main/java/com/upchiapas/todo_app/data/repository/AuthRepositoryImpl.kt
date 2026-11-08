package com.upchiapas.todo_app.data.repository

import com.upchiapas.todo_app.core.network.NetworkErrorMapper
import com.upchiapas.todo_app.core.network.mapFailure
import com.upchiapas.todo_app.core.session.SessionManager
import com.upchiapas.todo_app.data.datasource.AuthApiService
import com.upchiapas.todo_app.data.dto.RegisterRequestDto
import com.upchiapas.todo_app.data.mapper.toDomain
import com.upchiapas.todo_app.domain.model.User
import com.upchiapas.todo_app.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return runCatching {
            val dto = RegisterRequestDto(
                name = name.ifBlank { null },
                email = email,
                password = password
            )
            apiService.register(dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<String> {
        return runCatching {
            val tokenDto = apiService.login(email = email, pass = password)
            sessionManager.saveToken(tokenDto.accessToken)
            tokenDto.accessToken
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override fun logout() {
        sessionManager.clearSession()
    }

    override fun hasActiveSession(): Boolean {
        return sessionManager.hasActiveSession()
    }
}
