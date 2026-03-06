package com.hustle.bankapp.ui.transfer

data class TransferUiState(
    val recipientId: String = "",
    val amountString: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val amountAsDouble: Double
        get() = amountString.toDoubleOrNull() ?: 0.0
}
