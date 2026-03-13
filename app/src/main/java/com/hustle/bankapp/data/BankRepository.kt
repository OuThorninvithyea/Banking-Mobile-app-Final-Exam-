package com.hustle.bankapp.data

import kotlinx.coroutines.flow.Flow

interface BankRepository {
    fun getBalance(): Flow<Double>
    fun getTransactions(): Flow<List<Transaction>>
    fun getUserProfile(): Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun processTransfer(amount: Double, recipientId: String, senderAccountId: String = ""): Result<Unit>
    suspend fun deposit(amount: Double): Result<Unit>
    suspend fun withdraw(amount: Double): Result<Unit>
    suspend fun registerUser(name: String, email: String, password: String): Result<User>
    suspend fun updateProfile(name: String, email: String, contact: String): Result<Unit>
    suspend fun logout(): Result<Unit>

    // Cards
    fun getCards(): Flow<List<Card>>
    suspend fun createCard(): Result<Card>
    suspend fun toggleFreezeCard(cardId: String): Result<Card>
    suspend fun updateCardLimit(cardId: String, limit: Double): Result<Card>
    suspend fun updateCardInfo(cardId: String, type: String): Result<Card>

    // Accounts
    fun getAccounts(): Flow<List<Account>>
    suspend fun createAccount(name: String, type: AccountType): Result<Account>

    // Favorites (Contacts)
    fun getFavorites(): Flow<List<Contact>>
    suspend fun addFavorite(name: String, accountNumber: String): Result<Contact>
    suspend fun removeFavorite(contactId: String): Result<Unit>
}
