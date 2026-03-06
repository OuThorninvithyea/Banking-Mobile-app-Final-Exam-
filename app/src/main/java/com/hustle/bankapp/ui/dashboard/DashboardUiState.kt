package com.hustle.bankapp.ui.dashboard

import com.hustle.bankapp.data.Transaction

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val balance: Double,
        val recentTransactions: List<Transaction>,
        val chartData: List<Float>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
