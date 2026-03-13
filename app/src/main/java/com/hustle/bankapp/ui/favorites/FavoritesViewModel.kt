package com.hustle.bankapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.Contact
import com.hustle.bankapp.data.BankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val contacts: List<Contact>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel(
    private val repository: BankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.getFavorites()
                .catch { e -> _uiState.value = FavoritesUiState.Error(e.message ?: "Unknown error") }
                .collect { contacts ->
                    _uiState.value = FavoritesUiState.Success(contacts)
                }
        }
    }

    fun addFavorite(name: String, accountNumber: String) {
        viewModelScope.launch {
            repository.addFavorite(name, accountNumber)
                .onSuccess { loadFavorites() }
                .onFailure { e -> _uiState.value = FavoritesUiState.Error(e.message ?: "Failed to add favorite") }
        }
    }

    fun removeFavorite(contactId: String) {
        viewModelScope.launch {
            repository.removeFavorite(contactId)
                .onSuccess { loadFavorites() }
                .onFailure { e -> _uiState.value = FavoritesUiState.Error(e.message ?: "Failed to remove favorite") }
        }
    }
}
