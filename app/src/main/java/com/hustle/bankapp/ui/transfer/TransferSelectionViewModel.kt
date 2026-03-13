package com.hustle.bankapp.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.Contact
import com.hustle.bankapp.data.BankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TransferSelectionUiState {
    object Loading : TransferSelectionUiState()
    data class Success(val favorites: List<Contact>) : TransferSelectionUiState()
    data class Error(val message: String) : TransferSelectionUiState()
}

class TransferSelectionViewModel(
    private val repository: BankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransferSelectionUiState>(TransferSelectionUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.getFavorites()
                .catch { e -> _uiState.value = TransferSelectionUiState.Error(e.message ?: "Unknown error") }
                .collect { favorites ->
                    _uiState.value = TransferSelectionUiState.Success(favorites)
                }
        }
    }
}
