package com.hustle.bankapp.data

import com.google.gson.annotations.SerializedName
data class User(
    val id: String,
    val name: String,
    @SerializedName("account_number") val accountNumber: String,
    val balance: Double,
    val email: String = "",
    val contact: String = ""
)
