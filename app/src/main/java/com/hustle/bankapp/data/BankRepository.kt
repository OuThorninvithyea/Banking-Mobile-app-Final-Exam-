package com.hustle.bankapp.data

import kotlinx.coroutines.flow.Flow

interface BankRepository {
    fun getBalance(): Flow<Double>
    fun getTransactions(): Flow<List<Transaction>>
    fun getUserProfile(): Flow<User?>
    suspend fun processTransfer(amount: Double, recipientId: String): Result<Unit>
    suspend fun deposit(amount: Double): Result<Unit>
    suspend fun withdraw(amount: Double): Result<Unit>
    suspend fun registerUser(name: String, email: String, password: String): Result<User>
    suspend fun updateProfile(name: String, email: String, contact: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
