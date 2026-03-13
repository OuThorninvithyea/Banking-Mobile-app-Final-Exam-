package com.hustle.bankapp.data.api

import com.google.gson.annotations.SerializedName
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.User
import com.hustle.bankapp.data.Card
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// ── Request DTOs ──────────────────────────────────────────────────────

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TransferRequest(
    val amount: Double,
    @SerializedName("recipient_id") val recipientId: String,
    @SerializedName("sender_account_id") val senderAccountId: String = ""
)

data class DepositRequest(val amount: Double)

data class WithdrawRequest(val amount: Double)

data class LimitRequest(val limit: Double)

data class EditCardRequest(val type: String)

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val contact: String
)

data class CreateAccountRequest(
    val name: String,
    val type: String
)

data class EditAccountRequest(
    val name: String? = null,
    val type: String? = null
)

data class AccountTransferRequest(
    @SerializedName("from_account_id") val fromAccountId: String,
    @SerializedName("to_account_id") val toAccountId: String,
    val amount: Double
)

data class AddFavoriteRequest(
    val name: String,
    @SerializedName("account_number") val accountNumber: String
)

data class LinkAccountRequest(
    @SerializedName("account_id") val accountId: String
)

// ── Response DTOs ─────────────────────────────────────────────────────

data class AuthResponse(
    val token: String,
    val user: User
)

data class BalanceResponse(
    val balance: Double
)

data class TransactionListResponse(
    val transactions: List<Transaction>
)

data class MessageResponse(
    val message: String
)

// ── Retrofit API Service ──────────────────────────────────────────────

interface BankApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // User
    @GET("api/user/balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<User>

    @POST("api/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<MessageResponse>

    // Transactions
    @POST("api/transactions/transfer")
    suspend fun transfer(@Body request: TransferRequest): Response<MessageResponse>

    @POST("api/transactions/deposit")
    suspend fun deposit(@Body request: DepositRequest): Response<MessageResponse>

    @POST("api/transactions/withdraw")
    suspend fun withdraw(@Body request: WithdrawRequest): Response<MessageResponse>

    @GET("api/transactions/history")
    suspend fun getTransactionHistory(): Response<TransactionListResponse>

    // Cards
    @GET("api/cards")
    suspend fun getCards(): Response<List<Card>>

    @POST("api/cards")
    suspend fun createCard(): Response<Card>

    @PUT("api/cards/{id}/freeze")
    suspend fun toggleFreezeCard(@Path("id") cardId: String): Response<Card>

    @PUT("api/cards/{id}/limit")
    suspend fun updateCardLimit(@Path("id") cardId: String, @Body request: LimitRequest): Response<Card>

    @PUT("api/cards/{id}/edit")
    suspend fun updateCardInfo(@Path("id") cardId: String, @Body request: EditCardRequest): Response<Card>

    @PUT("api/cards/{id}/link")
    suspend fun linkCardToAccount(@Path("id") cardId: String, @Body request: LinkAccountRequest): Response<Card>

    // Accounts
    @GET("api/accounts")
    suspend fun getAccounts(): Response<List<com.hustle.bankapp.data.Account>>

    @POST("api/accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): Response<com.hustle.bankapp.data.Account>

    @PUT("api/accounts/{id}")
    suspend fun editAccount(@Path("id") accountId: String, @Body request: EditAccountRequest): Response<com.hustle.bankapp.data.Account>

    @retrofit2.http.DELETE("api/accounts/{id}")
    suspend fun deleteAccount(@Path("id") accountId: String): Response<MessageResponse>

    @POST("api/accounts/transfer")
    suspend fun transferBetweenAccounts(@Body request: AccountTransferRequest): Response<MessageResponse>

    // Favorites
    @GET("api/favorites")
    suspend fun getFavorites(): Response<List<com.hustle.bankapp.data.Contact>>

    @POST("api/favorites")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<com.hustle.bankapp.data.Contact>

    @retrofit2.http.DELETE("api/favorites/{id}")
    suspend fun removeFavorite(@Path("id") contactId: String): Response<MessageResponse>
}
