package com.hustle.bankapp.data

import com.google.gson.annotations.SerializedName

enum class TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val timestamp: String,
    @SerializedName("recipient_id") val recipientId: String? = null,
    val category: String = "General"
)
