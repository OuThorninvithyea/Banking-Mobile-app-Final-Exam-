package com.hustle.bankapp.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DepositWithdrawUiState(
    val amountString: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val amountAsDouble: Double get() = amountString.toDoubleOrNull() ?: 0.0
}

class DepositWithdrawViewModel(
    private val repository: BankRepository,
    val transactionType: TransactionType // DEPOSIT or WITHDRAW
) : ViewModel() {

    private val _uiState = MutableStateFlow(DepositWithdrawUiState())
    val uiState: StateFlow<DepositWithdrawUiState> = _uiState.asStateFlow()

    fun updateAmount(input: String) {
        val currentStr = _uiState.value.amountString
        if (input == "DELETE") {
            if (currentStr.isNotEmpty()) {
                _uiState.update { it.copy(amountString = currentStr.dropLast(1), error = null) }
            }
            return
        }
        if (input == "." && currentStr.contains(".")) return
        val newStr = if (currentStr == "0" && input != ".") input else currentStr + input
        if (newStr.substringAfter(".", "").length > 2) return
        _uiState.update { it.copy(amountString = newStr, error = null) }
    }

    fun submit() {
        val amount = _uiState.value.amountAsDouble
        if (amount <= 0) {
            _uiState.update { it.copy(error = "Amount must be greater than $0.00.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = when (transactionType) {
                TransactionType.DEPOSIT -> repository.deposit(amount)
                TransactionType.WITHDRAW -> {
                    // Extra validation: check balance before calling repository
                    val balance = repository.getBalance().first()
                    if (amount > balance) {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Insufficient funds. Available: $$balance")
                        }
                        return@launch
                    }
                    repository.withdraw(amount)
                }
                else -> Result.failure(Exception("Unsupported transaction type."))
            }

            result
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { ex -> _uiState.update { it.copy(isLoading = false, error = ex.message) } }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
