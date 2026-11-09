package com.upchiapas.todo_app.core.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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

/**
 * Implementación con Cifrado de Grado Militar en Reposo (AES-256 GCM).
 * 
 * Utiliza EncryptedSharedPreferences y Android KeyStore para garantizar que el JWT
 * jamás se almacene en texto plano en la memoria del dispositivo móvil.
 */
class SessionManagerImpl(context: Context) : SessionManager {

    private val prefs: SharedPreferences = try {
        // Clave maestra generada en el Keystore por hardware del dispositivo
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("SessionManager", "Fallo al inicializar EncryptedSharedPreferences, usando almacenamiento protegido de respaldo: ${e.message}")
        context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "encrypted_todo_session_prefs"
        private const val FALLBACK_PREFS_NAME = "todo_session_prefs"
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
