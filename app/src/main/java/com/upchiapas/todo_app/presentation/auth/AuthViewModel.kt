package com.upchiapas.todo_app.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.todo_app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = authRepository.hasActiveSession())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(nameInput = value, nameError = null, generalError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(emailInput = value, emailError = null, generalError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value, passwordError = null, generalError = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPasswordInput = value, confirmPasswordError = null, generalError = null) }
    }

    fun login() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        var hasError = false

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "El correo no puede estar vacío.") }
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Ingresa un formato de correo válido.") }
            hasError = true
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "La contraseña no puede estar vacía.") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            userMessage = "¡Inicio de sesión exitoso!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = error.localizedMessage ?: "Credenciales inválidas o error de red"
                        )
                    }
                }
            )
        }
    }

    fun register() {
        val name = _uiState.value.nameInput.trim()
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput
        val confirmPassword = _uiState.value.confirmPasswordInput

        var hasError = false

        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre es obligatorio.") }
            hasError = true
        }

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "El correo no puede estar vacío.") }
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Ingresa un correo electrónico válido.") }
            hasError = true
        }

        if (password.length < 8) {
            _uiState.update { it.copy(passwordError = "La contraseña debe tener mínimo 8 caracteres.") }
            hasError = true
        }

        if (confirmPassword != password) {
            _uiState.update { it.copy(confirmPasswordError = "Las contraseñas no coinciden.") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            val result = authRepository.register(name, email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegisteredSuccess = true,
                            userMessage = "Cuenta creada para ${user.email}. Por favor inicia sesión."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = error.localizedMessage ?: "Error al registrar el usuario"
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            AuthUiState(
                isAuthenticated = false,
                userMessage = "Sesión cerrada correctamente"
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun resetRegisteredStatus() {
        _uiState.update { it.copy(isRegisteredSuccess = false) }
    }
}
