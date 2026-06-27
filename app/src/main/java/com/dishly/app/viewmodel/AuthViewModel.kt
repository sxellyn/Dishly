package com.dishly.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dishly.app.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val navigateToMain: Boolean = false,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordUsername: String = "",
    val forgotPasswordError: String? = null,
    val forgotPasswordMessage: String? = null,
    val isResetLoading: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onUsernameChange(v: String) {
        _uiState.value = _uiState.value.copy(username = v, error = null)
    }

    fun onEmailChange(v: String) {
        _uiState.value = _uiState.value.copy(email = v, error = null)
    }

    fun onPasswordChange(v: String) {
        _uiState.value = _uiState.value.copy(password = v, error = null)
    }

    fun onConfirmPasswordChange(v: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = v, error = null)
    }

    fun openForgotPasswordDialog() {
        val state = _uiState.value
        _uiState.value = state.copy(
            showForgotPasswordDialog = true,
            forgotPasswordUsername = state.username,
            forgotPasswordError = null,
            forgotPasswordMessage = null
        )
    }

    fun closeForgotPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showForgotPasswordDialog = false,
            forgotPasswordError = null,
            forgotPasswordMessage = null,
            isResetLoading = false
        )
    }

    fun onForgotPasswordUsernameChange(v: String) {
        _uiState.value = _uiState.value.copy(
            forgotPasswordUsername = v,
            forgotPasswordError = null,
            forgotPasswordMessage = null
        )
    }

    fun sendPasswordReset() {
        val state = _uiState.value
        if (state.forgotPasswordUsername.isBlank()) {
            _uiState.value = state.copy(forgotPasswordError = "Please enter your username")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isResetLoading = true, forgotPasswordError = null)
            val result = AuthRepository.sendPasswordReset(state.forgotPasswordUsername)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isResetLoading = false,
                    forgotPasswordMessage = "Password reset email sent. Check your inbox."
                )
            } else {
                _uiState.value.copy(
                    isResetLoading = false,
                    forgotPasswordError = AuthRepository.mapAuthError(result.exceptionOrNull()!!)
                )
            }
        }
    }

    fun signIn() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in your username and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = AuthRepository.signIn(getApplication(), state.username, state.password)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, navigateToMain = true)
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = AuthRepository.mapAuthError(result.exceptionOrNull()!!)
                )
            }
        }
    }

    fun signUp() {
        val state = _uiState.value
        if (state.username.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in all fields")
            return
        }
        if (!state.email.contains("@")) {
            _uiState.value = state.copy(error = "Please enter a valid email address")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = AuthRepository.signUp(state.username, state.email, state.password)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, navigateToMain = true)
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = AuthRepository.mapAuthError(result.exceptionOrNull()!!)
                )
            }
        }
    }

    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(navigateToMain = false)
    }
}
