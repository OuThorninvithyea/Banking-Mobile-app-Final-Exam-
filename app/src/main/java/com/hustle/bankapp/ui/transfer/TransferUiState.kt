package com.hustle.bankapp.ui.transfer

import com.hustle.bankapp.data.Account

data class TransferUiState(
    val recipientId: String = "",
    val amountString: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val availableAccounts: List<Account> = emptyList(),
    val selectedSourceAccount: Account? = null
) {
    val amountAsDouble: Double
        get() = amountString.toDoubleOrNull() ?: 0.0
}
