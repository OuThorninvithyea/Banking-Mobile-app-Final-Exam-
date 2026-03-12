package com.hustle.bankapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.BankRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: BankRepository
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<DashboardUiState> = refreshTrigger
        .flatMapLatest {
            combine(
                repository.getBalance(),
                repository.getTransactions(),
                repository.getUserProfile()
            ) { balance, transactions, user ->
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
        .catch { emit(DashboardUiState.Error(it.message ?: "An unknown error occurred") as DashboardUiState) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading as DashboardUiState
        )

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }
}