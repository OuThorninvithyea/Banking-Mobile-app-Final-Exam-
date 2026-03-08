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
    @SerializedName("recipient_id") val recipientId: String
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
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // User
    @GET("/api/user/balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @GET("/api/user/profile")
    suspend fun getUserProfile(): Response<User>

    @POST("/api/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<MessageResponse>

    // Transactions
    @POST("/api/transactions/transfer")
    suspend fun transfer(@Body request: TransferRequest): Response<MessageResponse>

    @POST("/api/transactions/deposit")
    suspend fun deposit(@Body request: DepositRequest): Response<MessageResponse>

    @POST("/api/transactions/withdraw")
    suspend fun withdraw(@Body request: WithdrawRequest): Response<MessageResponse>

    @GET("/api/transactions/history")
    suspend fun getTransactionHistory(): Response<TransactionListResponse>

    // Cards
    @GET("/api/cards")
    suspend fun getCards(): Response<List<Card>>

    @POST("/api/cards")
    suspend fun createCard(): Response<Card>

    @PUT("/api/cards/{id}/freeze")
    suspend fun toggleFreezeCard(@Path("id") cardId: String): Response<Card>

    @PUT("/api/cards/{id}/limit")
    suspend fun updateCardLimit(@Path("id") cardId: String, @Body request: LimitRequest): Response<Card>

    @PUT("/api/cards/{id}/edit")
    suspend fun updateCardInfo(@Path("id") cardId: String, @Body request: EditCardRequest): Response<Card>
}
