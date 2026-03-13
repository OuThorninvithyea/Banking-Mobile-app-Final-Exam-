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
                timestamp = "2026-03-02T10:00:00Z"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.DEPOSIT,
                amount = 1500.0,
                timestamp = "2026-03-03T10:00:00Z"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.WITHDRAW,
                amount = 350.0,
                timestamp = "2026-03-04T10:00:00Z"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.TRANSFER,
                amount = 200.0,
                timestamp = "2026-03-05T10:00:00Z",
                recipientId = "user_ext_1"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.DEPOSIT,
                amount = 750.0,
                timestamp = "2026-03-06T10:00:00Z"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.WITHDRAW,
                amount = 150.0,
                timestamp = "2026-03-07T10:00:00Z"
            )
        )
    )

    // ── Reads ──────────────────────────────────────────────────────────

    override fun getBalance(): Flow<Double> = _userFlow.map { it?.balance ?: 0.0 }

    override fun getTransactions(): Flow<List<Transaction>> = _transactionsFlow.asStateFlow()

    override fun getUserProfile(): Flow<User?> = _userFlow.asStateFlow()

    // ── Login ────────────────────────────────────────────────────────────

    override suspend fun login(email: String, password: String): Result<User> {
        val user = _userFlow.value ?: return Result.failure(Exception("Invalid credentials."))
        return Result.success(user)
    }

    // ── Deposit / Withdraw ─────────────────────────────────────────────

    override suspend fun deposit(amount: Double): Result<Unit> {
        if (amount <= 0) return Result.failure(Exception("Deposit amount must be greater than \$0.00."))
        val user = _userFlow.value ?: return Result.failure(Exception("No active user."))
        _userFlow.value = user.copy(balance = user.balance + amount)
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.DEPOSIT,
            amount = amount,
            timestamp = "2026-03-08T10:00:00Z"
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
            timestamp = "2026-03-08T10:00:00Z"
        )
        _transactionsFlow.value = listOf(tx) + _transactionsFlow.value
        return Result.success(Unit)
    }

    // ── Transfer ───────────────────────────────────────────────────────

    override suspend fun processTransfer(amount: Double, recipientId: String, senderAccountId: String): Result<Unit> {
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
            timestamp = "2026-03-08T10:00:00Z",
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
                timestamp = "2026-03-08T10:00:00Z"
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

    // ── Cards (mock stubs) ─────────────────────────────────────────────

    private val _cardsFlow = MutableStateFlow<List<Card>>(emptyList())

    override fun getCards(): Flow<List<Card>> = _cardsFlow.asStateFlow()

    override suspend fun createCard(): Result<Card> {
        val card = Card(
            id = UUID.randomUUID().toString(),
            number = (1000000000000000L..9999999999999999L).random().toString(),
            expiry = "03/29",
            cvv = (100..999).random().toString(),
            isFrozen = false,
            type = "Virtual",
            limit = 10000.0
        )
        _cardsFlow.value = _cardsFlow.value + card
        return Result.success(card)
    }

    override suspend fun toggleFreezeCard(cardId: String): Result<Card> {
        val updated = _cardsFlow.value.map { if (it.id == cardId) it.copy(isFrozen = !it.isFrozen) else it }
        _cardsFlow.value = updated
        return updated.find { it.id == cardId }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Card not found"))
    }

    override suspend fun updateCardLimit(cardId: String, limit: Double): Result<Card> {
        val updated = _cardsFlow.value.map { if (it.id == cardId) it.copy(limit = limit) else it }
        _cardsFlow.value = updated
        return updated.find { it.id == cardId }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Card not found"))
    }

    override suspend fun updateCardInfo(cardId: String, type: String): Result<Card> {
        val updated = _cardsFlow.value.map { if (it.id == cardId) it.copy(type = type) else it }
        _cardsFlow.value = updated
        return updated.find { it.id == cardId }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Card not found"))
    }

    // ── Accounts ───────────────────────────────────────────────────────

    private val _accountsFlow = MutableStateFlow<List<Account>>(
        listOf(
            Account(
                id = "acc_1",
                accountNumber = "001-234-5678",
                accountName = "Main Account",
                balance = 5000.0,
                type = AccountType.CURRENT
            ),
            Account(
                id = "acc_2",
                accountNumber = "001-987-6543",
                accountName = "Savings",
                balance = 12500.75,
                type = AccountType.SAVINGS
            )
        )
    )

    override fun getAccounts(): Flow<List<Account>> = _accountsFlow.asStateFlow()

    override suspend fun createAccount(name: String, type: AccountType): Result<Account> {
        val raw = (1000000000L..9999999999L).random().toString()
        val accountNumber = "${raw.substring(0,3)}-${raw.substring(3,6)}-${raw.substring(6)}"
        
        val newAccount = Account(
            id = UUID.randomUUID().toString(),
            accountNumber = accountNumber,
            accountName = name,
            balance = 0.0,
            type = type
        )
        _accountsFlow.value = _accountsFlow.value + newAccount
        return Result.success(newAccount)
    }

    // ── Favorites ──────────────────────────────────────────────────────

    private val _favoritesFlow = MutableStateFlow<List<Contact>>(
        listOf(
            Contact(UUID.randomUUID().toString(), "Mom", "001-999-8888"),
            Contact(UUID.randomUUID().toString(), "Dad", "001-777-6666"),
            Contact(UUID.randomUUID().toString(), "Brother", "001-555-4444")
        )
    )

    override fun getFavorites(): Flow<List<Contact>> = _favoritesFlow.asStateFlow()

    override suspend fun addFavorite(name: String, accountNumber: String): Result<Contact> {
        val newContact = Contact(
            id = UUID.randomUUID().toString(),
            name = name,
            accountNumber = accountNumber
        )
        _favoritesFlow.value = _favoritesFlow.value + newContact
        return Result.success(newContact)
    }

    override suspend fun removeFavorite(contactId: String): Result<Unit> {
        _favoritesFlow.value = _favoritesFlow.value.filter { it.id != contactId }
        return Result.success(Unit)
    }
}
