package com.hustle.bankapp.data

import com.hustle.bankapp.data.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Real implementation of [BankRepository] that delegates to [BankApiService]
 * for all network operations. Every call is wrapped in try/catch so the
 * ViewModels always receive a [Result] and can show error UI instead of
 * crashing.
 */
class RemoteBankRepositoryImpl(
    private val api: BankApiService
) : BankRepository {

    // ── Reads (Flow-based) ────────────────────────────────────────────

    override fun getBalance(): Flow<Double> = flow {
        try {
            val response = api.getBalance()
            if (response.isSuccessful) {
                emit(response.body()?.balance ?: 0.0)
            } else {
                throw Exception("Failed to fetch balance: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}")
        }
    }

    override fun getTransactions(): Flow<List<Transaction>> = flow {
        try {
            val response = api.getTransactionHistory()
            if (response.isSuccessful) {
                emit(response.body()?.transactions ?: emptyList())
            } else {
                throw Exception("Failed to fetch transactions: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}")
        }
    }

    override fun getUserProfile(): Flow<User?> = flow {
        try {
            val response = api.getUserProfile()
            if (response.isSuccessful) {
                emit(response.body())
            } else {
                throw Exception("Failed to fetch profile: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}")
        }
    }

    // ── Transfer ──────────────────────────────────────────────────────

    override suspend fun processTransfer(amount: Double, recipientId: String): Result<Unit> {
        return try {
            val response = api.transfer(TransferRequest(amount, recipientId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Transfer failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Deposit / Withdraw ────────────────────────────────────────────

    override suspend fun deposit(amount: Double): Result<Unit> {
        return try {
            val response = api.deposit(DepositRequest(amount))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Deposit failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    override suspend fun withdraw(amount: Double): Result<Unit> {
        return try {
            val response = api.withdraw(WithdrawRequest(amount))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Withdrawal failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────

    override suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        return try {
            val response = api.register(RegisterRequest(name, email, password))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Empty response from server."))
                Result.success(body.user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Registration failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Profile ──────────────────────────────────────────────────────

    override suspend fun updateProfile(name: String, email: String, contact: String): Result<Unit> {
        return try {
            val response = api.updateProfile(UpdateProfileRequest(name, email, contact))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Profile update failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Logout ────────────────────────────────────────────────────────

    override suspend fun logout(): Result<Unit> {
        // Logout is typically a local-only operation (clear token, etc.)
        // If your backend has a logout endpoint, call it here.
        return Result.success(Unit)
    }
}
