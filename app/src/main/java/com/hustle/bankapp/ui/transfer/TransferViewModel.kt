package com.hustle.bankapp.ui.transfer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.Account
import com.hustle.bankapp.data.AccountType
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

    private val _uiState: MutableStateFlow<TransferUiState>
    val uiState: StateFlow<TransferUiState>

    init {
        val parsed = parseQrRecipient(initialRecipient)
        _uiState = MutableStateFlow(
            TransferUiState(
                recipientId = parsed.recipientId,
                amountString = parsed.amount
            )
        )
        uiState = _uiState.asStateFlow()

        loadAccounts()
    }

    private data class ParsedQr(val recipientId: String, val amount: String)

    private fun parseQrRecipient(raw: String): ParsedQr {
        if (raw.startsWith("hustlebank://")) {
            try {
                val uri = Uri.parse(raw)
                val id = uri.getQueryParameter("id").orEmpty()
                val amount = uri.getQueryParameter("amount").orEmpty()
                return ParsedQr(recipientId = id, amount = amount)
            } catch (_: Exception) { }
        }
        return ParsedQr(recipientId = raw, amount = "")
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            repository.getAccounts()
                .catch { _ -> emit(emptyList()) }
                .collect { accounts ->
                    if (accounts.isNotEmpty()) {
                        _uiState.update { state ->
                            state.copy(
                                availableAccounts = accounts,
                                selectedSourceAccount = state.selectedSourceAccount ?: accounts.firstOrNull()
                            )
                        }
                    } else {
                        buildPrimaryAccountFallback()
                    }
                }
        }
    }

    private suspend fun buildPrimaryAccountFallback() {
        try {
            val profile = repository.getUserProfile().catch { emit(null) }.first()
            val balance = try { repository.getBalance().first() } catch (_: Exception) { 0.0 }
            if (profile != null) {
                val primary = Account(
                    id = profile.id,
                    accountNumber = profile.accountNumber,
                    accountName = "Primary Account",
                    balance = balance,
                    type = AccountType.CURRENT
                )
                _uiState.update { state ->
                    state.copy(
                        availableAccounts = listOf(primary),
                        selectedSourceAccount = state.selectedSourceAccount ?: primary
                    )
                }
            }
        } catch (_: Exception) { }
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
                repository.processTransfer(amount, recipientId, senderId)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                    .onFailure { exception ->
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
