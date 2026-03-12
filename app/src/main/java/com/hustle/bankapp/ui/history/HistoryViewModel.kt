package com.hustle.bankapp.ui.history

import androidx.lifecycle.ViewModel
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class HistoryViewModel(repository: BankRepository) : ViewModel() {
    val transactions: Flow<List<Transaction>> = repository.getTransactions()
        .catch { emit(emptyList()) }
}
