package com.hustle.bankapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.BankRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: BankRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getBalance(),
        repository.getTransactions()
    ) { balance, transactions ->
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
            chartData = chartData
        ) as DashboardUiState
    }
        .catch { emit(DashboardUiState.Error(it.message ?: "An unknown error occurred") as DashboardUiState) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading as DashboardUiState
        )
}
