package com.hustle.bankapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class IncomingMoneyEvent(
    val amount: Double,
    val transactionId: String
)

class DashboardViewModel(
    private val repository: BankRepository
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val USE_MOCK_DATA = false

    private val knownTransactionIds = mutableSetOf<String>()
    private var isFirstLoad = true

    private val _incomingMoneyEvent = MutableStateFlow<IncomingMoneyEvent?>(null)
    val incomingMoneyEvent: StateFlow<IncomingMoneyEvent?> = _incomingMoneyEvent.asStateFlow()

    fun dismissIncomingMoney() {
        _incomingMoneyEvent.value = null
    }

    val uiState: StateFlow<DashboardUiState> = refreshTrigger
        .flatMapLatest {
            if (USE_MOCK_DATA) {
                flow {
                    emit(DashboardUiState.Success(
                        balance = 12500.75,
                        recentTransactions = listOf(
                            Transaction("1", TransactionType.WITHDRAW, 50.0, "2024-03-13T10:00:00.000000Z"),
                            Transaction("2", TransactionType.DEPOSIT, 1200.0, "2024-03-12T15:30:00.000000Z"),
                            Transaction("3", TransactionType.TRANSFER, 25.50, "2024-03-11T09:15:00.000000Z")
                        ),
                        chartData = listOf(11000f, 11500f, 12000f, 11800f, 12500f, 12200f, 12500f),
                        userName = "Thorninvithyea"
                    ) as DashboardUiState)
                }
            } else {
                combine(
                    repository.getBalance(),
                    repository.getTransactions(),
                    repository.getUserProfile()
                ) { balance, transactions, user ->
                    detectNewIncoming(transactions)

                    val chartData = listOf(
                        (balance * 0.95f).toFloat(),
                        (balance * 0.92f).toFloat(),
                        (balance * 0.98f).toFloat(),
                        (balance * 0.97f).toFloat(),
                        (balance * 1.01f).toFloat(),
                        (balance * 0.99f).toFloat(),
                        balance.toFloat()
                    )

                    DashboardUiState.Success(
                        balance = balance,
                        recentTransactions = transactions.take(5),
                        chartData = chartData,
                        userName = user?.name ?: ""
                    ) as DashboardUiState
                }
            }
        }
        .catch { emit(DashboardUiState.Error(it.message ?: "An unknown error occurred") as DashboardUiState) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading as DashboardUiState
        )

    private fun detectNewIncoming(transactions: List<Transaction>) {
        val currentIds = transactions.map { it.id }.toSet()
        if (isFirstLoad) {
            knownTransactionIds.addAll(currentIds)
            isFirstLoad = false
            return
        }
        val newDeposits = transactions.filter { tx ->
            tx.id !in knownTransactionIds && tx.type == TransactionType.DEPOSIT
        }
        knownTransactionIds.addAll(currentIds)
        if (newDeposits.isNotEmpty()) {
            val total = newDeposits.sumOf { it.amount }
            _incomingMoneyEvent.value = IncomingMoneyEvent(
                amount = total,
                transactionId = newDeposits.first().id
            )
        }
    }

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }
}