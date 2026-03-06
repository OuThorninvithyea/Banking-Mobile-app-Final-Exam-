package com.hustle.bankapp.data

data class User(
    val id: String,
    val name: String,
    val accountNumber: String,
    val balance: Double,
    val email: String = "",
    val contact: String = ""
)
