package com.hustle.bankapp.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.Account
import com.hustle.bankapp.data.BankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransferViewModel(
    private val repository: BankRepository,
    initialRecipient: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState(recipientId = initialRecipient))
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAccounts()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to load accounts") }
                }
                .collect { accounts ->
                    _uiState.update { state ->
                        state.copy(
                            availableAccounts = accounts,
                            selectedSourceAccount = state.selectedSourceAccount ?: accounts.firstOrNull()
                        )
                    }
                }
        }
    }

    fun selectSourceAccount(account: Account) {
        _uiState.update { it.copy(selectedSourceAccount = account) }
    }

    fun updateRecipientId(id: String) {
        _uiState.update { it.copy(recipientId = id, error = null) }
    }

    fun updateAmount(input: String) {
        val currentStr = _uiState.value.amountString
        
        // Handle deletion
        if (input == "DELETE") {
            if (currentStr.isNotEmpty()) {
                _uiState.update { it.copy(amountString = currentStr.dropLast(1), error = null) }
            }
            return
        }

        // Prevent multiple decimals
        if (input == "." && currentStr.contains(".")) return

        // Prevent leading zeros unless it's a decimal
        val newStr = if (currentStr == "0" && input != ".") input else currentStr + input

        // Limit to 2 decimal places
        if (newStr.substringAfter(".", "").length > 2) return

        _uiState.update { it.copy(amountString = newStr, error = null) }
    }

    fun submitTransfer() {
        val currentState = _uiState.value
        val amount = currentState.amountAsDouble
        val recipientId = currentState.recipientId.trim()

        if (recipientId.isEmpty()) {
            _uiState.update { it.copy(error = "Recipient ID cannot be blank.") }
            return
        }

        if (amount <= 0) {
            _uiState.update { it.copy(error = "Amount must be greater than $0.00.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val currentBalance = repository.getBalance().first()
                if (amount > currentBalance) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Insufficient funds. Available balance: $$currentBalance"
                        )
                    }
                    return@launch
                }

                val senderId = currentState.selectedSourceAccount?.id ?: ""
                val result = repository.processTransfer(amount, recipientId, senderId)

                result.onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                result.onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.message}") }
            }
        }
    }
    
    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
