package com.hustle.bankapp.data

import com.google.gson.annotations.SerializedName

enum class AccountType {
    CURRENT,
    SAVINGS,
    FIXED_DEPOSIT,
    BUSINESS
}

data class Account(
    val id: String,
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("account_name") val accountName: String,
    val balance: Double,
    val type: AccountType,
    val currency: String = "USD"
)
