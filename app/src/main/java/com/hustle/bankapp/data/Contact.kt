package com.hustle.bankapp.data

data class Contact(
    val id: String,
    val name: String,
    val accountNumber: String,
    val bankName: String = "HustleBank",
    val profileImage: String? = null
)
