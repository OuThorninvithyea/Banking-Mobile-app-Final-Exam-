package com.hustle.bankapp.data

import com.google.gson.annotations.SerializedName

data class Contact(
    val id: String,
    val name: String,
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("bank_name") val bankName: String = "HustleBank",
    @SerializedName("profile_image") val profileImage: String? = null
)
