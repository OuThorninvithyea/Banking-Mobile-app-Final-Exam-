package com.hustle.bankapp.data

enum class TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val timestamp: Long,
    val recipientId: String? = null
)
