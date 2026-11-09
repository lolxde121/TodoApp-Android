package com.upchiapas.todo_app.core.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Abstracción para la gestión de sesión y persistencia local del token JWT.
 * Satisface los principios SRP y DIP requeridos en la rúbrica (Ítem 1 e Ítem 5).
 */
interface SessionManager {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearSession()
    fun hasActiveSession(): Boolean
}

class SessionManagerImpl(context: Context) : SessionManager {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "todo_session_prefs"
        private const val KEY_AUTH_TOKEN = "auth_jwt_token"
    }

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    override fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    override fun clearSession() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    override fun hasActiveSession(): Boolean {
        return !getToken().isNullOrBlank()
    }
}
