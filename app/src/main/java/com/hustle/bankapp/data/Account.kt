package com.hustle.bankapp.data

enum class AccountType {
    CURRENT,
    SAVINGS,
    FIXED_DEPOSIT,
    BUSINESS
}

data class Account(
    val id: String,
    val accountNumber: String,
    val accountName: String,
    val balance: Double,
    val type: AccountType,
    val currency: String = "USD"
)
