package com.upchiapas.todo_app.core.network

import com.upchiapas.todo_app.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp que inyecta automáticamente el token JWT en las cabeceras HTTP:
 * Authorization: Bearer <token>
 * 
 * Requisito de la rúbrica (Ítem 1 y Contrato mínimo de la API).
 */
class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = sessionManager.getToken()
        if (token.isNullOrBlank() || originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
