package com.upchiapas.todo_app.presentation.auth

/**
 * Estado inmutable de la UI para el flujo de Autenticación (Login y Registro).
 * Rúbrica Ítem 3: UiState inmutable que sobrevive a la rotación.
 */
data class AuthUiState(
    val nameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val userMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val isRegisteredSuccess: Boolean = false
)
