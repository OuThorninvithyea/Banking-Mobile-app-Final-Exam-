package com.hustle.bankapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registeredUser: User? = null   // non-null = success
)

class RegisterViewModel(private val repository: BankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String)            = _uiState.update { it.copy(name = v, error = null) }
    fun onEmailChange(v: String)           = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String)        = _uiState.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, error = null) }

    fun register() {
        val s = _uiState.value

        // Client-side validation
        when {
            s.name.isBlank() || s.email.isBlank() || s.password.isBlank() ->
                _uiState.update { it.copy(error = "Please fill in all fields.") }
            !s.email.contains("@") ->
                _uiState.update { it.copy(error = "Please enter a valid email address.") }
            s.password.length < 4 ->
                _uiState.update { it.copy(error = "Password must be at least 4 characters.") }
            s.password != s.confirmPassword ->
                _uiState.update { it.copy(error = "Passwords do not match.") }
            else -> {
                _uiState.update { it.copy(isLoading = true, error = null) }
                viewModelScope.launch {
                    repository.registerUser(s.name.trim(), s.email.trim(), s.password)
                        .onSuccess { user ->
                            _uiState.update { it.copy(isLoading = false, registeredUser = user) }
                        }
                        .onFailure { ex ->
                            _uiState.update { it.copy(isLoading = false, error = ex.message) }
                        }
                }
            }
        }
    }
}
