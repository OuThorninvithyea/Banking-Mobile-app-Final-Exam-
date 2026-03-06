package com.hustle.bankapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MockBankRepositoryImpl : BankRepository {

    private val _userFlow = MutableStateFlow<User?>(
        User(
            id = "user_1",
            name = "Test User",
            accountNumber = "001-234-5678",
            balance = 5000.0,
            email = "test@hustlebank.com",
            contact = "+1 555-000-1234"
        )
    )

    private val _transactionsFlow = MutableStateFlow(
        listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.DEPOSIT,
                amount = 2000.0,
                timestamp = System.currentTimeMillis() - 86400000 * 6
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.DEPOSIT,
                amount = 1500.0,
                timestamp = System.currentTimeMillis() - 86400000 * 5
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.WITHDRAW,
                amount = 350.0,
                timestamp = System.currentTimeMillis() - 86400000 * 4
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.TRANSFER,
                amount = 200.0,
                timestamp = System.currentTimeMillis() - 86400000 * 3,
                recipientId = "user_ext_1"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.DEPOSIT,
                amount = 750.0,
                timestamp = System.currentTimeMillis() - 86400000 * 2
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.WITHDRAW,
                amount = 150.0,
                timestamp = System.currentTimeMillis() - 86400000 * 1
            )
        )
    )

    // ── Reads ──────────────────────────────────────────────────────────

    override fun getBalance(): Flow<Double> = _userFlow.map { it?.balance ?: 0.0 }

    override fun getTransactions(): Flow<List<Transaction>> = _transactionsFlow.asStateFlow()

    override fun getUserProfile(): Flow<User?> = _userFlow.asStateFlow()

    // ── Deposit / Withdraw ─────────────────────────────────────────────

    override suspend fun deposit(amount: Double): Result<Unit> {
        if (amount <= 0) return Result.failure(Exception("Deposit amount must be greater than \$0.00."))
        val user = _userFlow.value ?: return Result.failure(Exception("No active user."))
        _userFlow.value = user.copy(balance = user.balance + amount)
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.DEPOSIT,
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        _transactionsFlow.value = listOf(tx) + _transactionsFlow.value
        return Result.success(Unit)
    }

    override suspend fun withdraw(amount: Double): Result<Unit> {
        if (amount <= 0) return Result.failure(Exception("Withdrawal amount must be greater than \$0.00."))
        val user = _userFlow.value ?: return Result.failure(Exception("No active user."))
        if (amount > user.balance) return Result.failure(
            Exception("Insufficient funds. Available: \$${user.balance}")
        )
        _userFlow.value = user.copy(balance = user.balance - amount)
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.WITHDRAW,
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        _transactionsFlow.value = listOf(tx) + _transactionsFlow.value
        return Result.success(Unit)
    }

    // ── Transfer ───────────────────────────────────────────────────────

    override suspend fun processTransfer(amount: Double, recipientId: String): Result<Unit> {
        val user = _userFlow.value ?: return Result.failure(Exception("No active user."))
        if (amount <= 0) return Result.failure(Exception("Transfer amount must be greater than 0."))
        if (user.balance < amount) return Result.failure(
            Exception("Insufficient funds. Available balance: \$${user.balance}")
        )
        _userFlow.value = user.copy(balance = user.balance - amount)
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.TRANSFER,
            amount = amount,
            timestamp = System.currentTimeMillis(),
            recipientId = recipientId
        )
        _transactionsFlow.value = listOf(tx) + _transactionsFlow.value
        return Result.success(Unit)
    }

    // ── Registration ───────────────────────────────────────────────────

    override suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("All fields required."))
        }
        val raw = (1000000000L..9999999999L).random().toString()
        val accountNumber = "${raw.substring(0,3)}-${raw.substring(3,6)}-${raw.substring(6)}"

        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = name,
            accountNumber = accountNumber,
            balance = 500.0,
            email = email
        )
        _userFlow.value = newUser
        _transactionsFlow.value = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.DEPOSIT,
                amount = 500.0,
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(newUser)
    }

    // ── Profile ────────────────────────────────────────────────────────

    override suspend fun updateProfile(name: String, email: String, contact: String): Result<Unit> {
        val user = _userFlow.value ?: return Result.failure(Exception("No active user."))
        _userFlow.value = user.copy(name = name, email = email, contact = contact)
        return Result.success(Unit)
    }

    // ── Logout ─────────────────────────────────────────────────────────

    override suspend fun logout(): Result<Unit> {
        _userFlow.value = null
        _transactionsFlow.value = emptyList()
        return Result.success(Unit)
    }
}
