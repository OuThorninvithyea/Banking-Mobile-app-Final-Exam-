package com.hustle.bankapp.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.Account
import com.hustle.bankapp.data.AccountType
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SpendingCategory(
    val name: String,
    val amount: Double,
    val percentage: Float
)

sealed class AccountsUiState {
    object Loading : AccountsUiState()
    data class Success(
        val accounts: List<Account>,
        val totalBalance: Double,
        val monthlySpent: Double = 0.0,
        val monthlyReceived: Double = 0.0,
        val transactionCount: Int = 0,
        val spendingByCategory: List<SpendingCategory> = emptyList(),
        val recentTransactions: List<Transaction> = emptyList()
    ) : AccountsUiState()
    data class Error(val message: String) : AccountsUiState()
}

class AccountsViewModel(
    private val repository: BankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountsUiState>(AccountsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            try {
                val profile = repository.getUserProfile().catch { }.first()
                val mainBalance = try {
                    repository.getBalance().catch { }.first()
                } catch (_: Exception) { 0.0 }

                val primaryAccount = if (profile != null) {
                    Account(
                        id = profile.id,
                        accountNumber = profile.accountNumber,
                        accountName = "Primary Account",
                        balance = mainBalance,
                        type = AccountType.CURRENT,
                        currency = "USD"
                    )
                } else null

                val createdAccounts = try {
                    repository.getAccounts().catch { }.first()
                } catch (_: Exception) { emptyList() }

                val allAccounts = listOfNotNull(primaryAccount) + createdAccounts
                val totalBalance = allAccounts.sumOf { it.balance }

                val transactions = try {
                    repository.getTransactions().catch { }.first()
                } catch (_: Exception) { emptyList() }

                val monthlySpent = transactions
                    .filter { it.type == TransactionType.TRANSFER || it.type == TransactionType.WITHDRAW }
                    .sumOf { it.amount }

                val monthlyReceived = transactions
                    .filter { it.type == TransactionType.DEPOSIT }
                    .sumOf { it.amount }

                val spendingByCategory = buildSpendingCategories(transactions)

                _uiState.value = AccountsUiState.Success(
                    accounts = allAccounts,
                    totalBalance = totalBalance,
                    monthlySpent = monthlySpent,
                    monthlyReceived = monthlyReceived,
                    transactionCount = transactions.size,
                    spendingByCategory = spendingByCategory,
                    recentTransactions = transactions.sortedByDescending { it.timestamp }.take(10)
                )
            } catch (e: Exception) {
                _uiState.value = AccountsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun buildSpendingCategories(transactions: List<Transaction>): List<SpendingCategory> {
        val outgoing = transactions.filter {
            it.type == TransactionType.TRANSFER || it.type == TransactionType.WITHDRAW
        }
        if (outgoing.isEmpty()) return emptyList()

        val totalSpent = outgoing.sumOf { it.amount }
        val grouped = outgoing.groupBy { it.category.ifBlank { "General" } }

        return grouped.map { (category, txns) ->
            val amount = txns.sumOf { it.amount }
            SpendingCategory(
                name = category,
                amount = amount,
                percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }
    }

    fun createAccount(name: String, type: AccountType) {
        viewModelScope.launch {
            repository.createAccount(name, type)
                .onSuccess { loadAll() }
                .onFailure { e -> _uiState.value = AccountsUiState.Error(e.message ?: "Failed to create account") }
        }
    }

    fun editAccount(accountId: String, name: String?, type: String?) {
        viewModelScope.launch {
            repository.editAccount(accountId, name, type)
                .onSuccess { loadAll() }
                .onFailure { e -> _uiState.value = AccountsUiState.Error(e.message ?: "Failed to edit account") }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
                .onSuccess { loadAll() }
                .onFailure { e -> _uiState.value = AccountsUiState.Error(e.message ?: "Failed to delete account") }
        }
    }
}
