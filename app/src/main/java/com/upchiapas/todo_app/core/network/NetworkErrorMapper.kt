package com.upchiapas.todo_app.core.network

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

object NetworkErrorMapper {

    fun map(throwable: Throwable): Throwable {
        return when (throwable) {
            is HttpException -> {
                val code = throwable.code()
                val errorBody = throwable.response()?.errorBody()?.string()
                val serverDetail = try {
                    if (!errorBody.isNullOrBlank()) {
                        val json = JsonParser.parseString(errorBody).asJsonObject
                        if (json.has("detail")) json.get("detail").asString else null
                    } else null
                } catch (_: Exception) {
                    null
                }

                val finalMessage = serverDetail ?: when (code) {
                    400 -> "Solicitud incorrecta: revisa los datos ingresados"
                    401 -> "Credenciales inválidas o sesión expirada"
                    404 -> "Recurso no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error HTTP $code: ${throwable.message()}"
                }
                Exception(finalMessage)
            }
            is IOException -> Exception("Error de conexión: verifica que el servidor esté activo y tengas internet")
            else -> throwable
        }
    }
}

/**
 * Extensión idéntica al repo kt_p4a para mapear fallos en el patrón Result<T>.
 */
fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) }
    )
}
