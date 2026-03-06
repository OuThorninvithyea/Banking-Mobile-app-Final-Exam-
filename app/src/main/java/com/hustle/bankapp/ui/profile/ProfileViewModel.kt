package com.hustle.bankapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val editContact: String = "",
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(private val repository: BankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getUserProfile().collect { user ->
                _uiState.update {
                    it.copy(
                        user = user,
                        editName = user?.name ?: "",
                        editEmail = user?.email ?: "",
                        editContact = user?.contact ?: ""
                    )
                }
            }
        }
    }

    fun toggleEdit() {
        val u = _uiState.value.user ?: return
        _uiState.update {
            it.copy(
                isEditing = !it.isEditing,
                editName = u.name,
                editEmail = u.email,
                editContact = u.contact,
                error = null
            )
        }
    }

    fun onNameChange(v: String)    = _uiState.update { it.copy(editName = v, error = null) }
    fun onEmailChange(v: String)   = _uiState.update { it.copy(editEmail = v, error = null) }
    fun onContactChange(v: String) = _uiState.update { it.copy(editContact = v, error = null) }

    fun saveProfile() {
        val s = _uiState.value
        if (s.editName.isBlank()) {
            _uiState.update { it.copy(error = "Name cannot be empty.") }
            return
        }
        viewModelScope.launch {
            repository.updateProfile(s.editName.trim(), s.editEmail.trim(), s.editContact.trim())
                .onSuccess { _uiState.update { it.copy(isEditing = false, error = null) } }
                .onFailure { ex -> _uiState.update { it.copy(error = ex.message) } }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
