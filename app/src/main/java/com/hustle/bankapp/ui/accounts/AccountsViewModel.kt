package com.hustle.bankapp.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.Account
import com.hustle.bankapp.data.AccountType
import com.hustle.bankapp.data.BankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AccountsUiState {
    object Loading : AccountsUiState()
    data class Success(
        val accounts: List<Account>,
        val totalBalance: Double
    ) : AccountsUiState()
    data class Error(val message: String) : AccountsUiState()
}

class AccountsViewModel(
    private val repository: BankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountsUiState>(AccountsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            repository.getAccounts()
                .catch { e -> _uiState.value = AccountsUiState.Error(e.message ?: "Unknown error") }
                .collect { accounts ->
                    _uiState.value = AccountsUiState.Success(
                        accounts = accounts,
                        totalBalance = accounts.sumOf { it.balance }
                    )
                }
        }
    }

    fun createAccount(name: String, type: AccountType) {
        viewModelScope.launch {
            repository.createAccount(name, type)
                .onSuccess { loadAccounts() }
                .onFailure { e -> _uiState.value = AccountsUiState.Error(e.message ?: "Failed to create account") }
        }
    }
}
