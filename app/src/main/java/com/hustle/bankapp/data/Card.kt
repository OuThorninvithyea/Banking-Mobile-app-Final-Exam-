package com.hustle.bankapp.data

import com.google.gson.annotations.SerializedName

data class Card(
    val id: String,
    val number: String,
    val expiry: String,
    val cvv: String,
    @SerializedName("is_frozen") val isFrozen: Boolean,
    val type: String,
    val limit: Double,
    @SerializedName("linked_account_id") val linkedAccountId: String? = null,
    @SerializedName("linked_account_name") val linkedAccountName: String? = null
)
