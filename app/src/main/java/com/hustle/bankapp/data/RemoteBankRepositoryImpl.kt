package com.hustle.bankapp.data

import com.hustle.bankapp.data.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

/**
 * Real implementation of [BankRepository] that delegates to [BankApiService]
 * for all network operations. Every call is wrapped in try/catch so the
 * ViewModels always receive a [Result] and can show error UI instead of
 * crashing.
 */
class RemoteBankRepositoryImpl(
    private val api: BankApiService,
    private val tokenManager: TokenManager
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
            throw Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}")
        }
    }

    // ── Login ───────────────────────────────────────────────────────────

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Empty response from server."))
                tokenManager.saveToken(body.token)
                Result.success(body.user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Login failed: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Transfer ──────────────────────────────────────────────────────

    override suspend fun processTransfer(amount: Double, recipientId: String, senderAccountId: String): Result<Unit> {
        return try {
            val response = api.transfer(TransferRequest(amount, recipientId, senderAccountId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Transfer failed: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
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
                tokenManager.saveToken(body.token)
                Result.success(body.user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Registration failed: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
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
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Logout ────────────────────────────────────────────────────────

    override suspend fun logout(): Result<Unit> {
        tokenManager.clearToken()
        return Result.success(Unit)
    }

    // ── Cards ─────────────────────────────────────────────────────────

    override fun getCards(): Flow<List<Card>> = flow {
        try {
            val response = api.getCards()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                throw Exception("Failed to fetch cards: ${response.message()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}")
        }
    }

    override suspend fun createCard(): Result<Card> {
        return try {
            val response = api.createCard()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to create card: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    override suspend fun toggleFreezeCard(cardId: String): Result<Card> {
        return try {
            val response = api.toggleFreezeCard(cardId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to toggle freeze: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    override suspend fun updateCardLimit(cardId: String, limit: Double): Result<Card> {
        return try {
            val response = api.updateCardLimit(cardId, LimitRequest(limit))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to update limit: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    override suspend fun updateCardInfo(cardId: String, type: String): Result<Card> {
        return try {
            val response = api.updateCardInfo(cardId, EditCardRequest(type))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to update card info: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Accounts ──────────────────────────────────────────────────────

    override fun getAccounts(): Flow<List<Account>> = flow {
        try {
            val response = api.getAccounts()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                throw Exception("Failed to fetch accounts: ${response.message()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}")
        }
    }

    override suspend fun createAccount(name: String, type: com.hustle.bankapp.data.AccountType): Result<Account> {
        return try {
            val response = api.createAccount(CreateAccountRequest(name, type.name))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to create account: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    // ── Favorites ──────────────────────────────────────────────────────

    override fun getFavorites(): Flow<List<Contact>> = flow {
        try {
            val response = api.getFavorites()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                throw Exception("Failed to fetch favorites: ${response.message()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}")
        }
    }

    override suspend fun addFavorite(name: String, accountNumber: String): Result<Contact> {
        return try {
            val response = api.addFavorite(AddFavoriteRequest(name, accountNumber))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to add favorite: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }

    override suspend fun removeFavorite(contactId: String): Result<Unit> {
        return try {
            val response = api.removeFavorite(contactId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Failed to remove favorite: $errorMsg"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        }
    }
}
