package com.hustle.bankapp.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val accountNumber: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel : ViewModel() {

    // Mock credentials — can be swapped for real backend later
    private val validAccount = "001-234-5678"
    private val validPassword = "1234"

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onAccountNumberChange(value: String) {
        _uiState.update { it.copy(accountNumber = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.accountNumber.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }

        // Simulate network delay using simple check
        if (state.accountNumber.trim() == validAccount && state.password == validPassword) {
            _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
        } else {
            _uiState.update {
                it.copy(isLoading = false, error = "Invalid account number or PIN. Please try again.")
            }
        }
    }

    fun resetAuth() {
        _uiState.update { AuthUiState() }
    }
}
