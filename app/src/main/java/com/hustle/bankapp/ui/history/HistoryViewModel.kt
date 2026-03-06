package com.hustle.bankapp.ui.history

import androidx.lifecycle.ViewModel
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.Transaction
import kotlinx.coroutines.flow.Flow

class HistoryViewModel(repository: BankRepository) : ViewModel() {
    val transactions: Flow<List<Transaction>> = repository.getTransactions()
}
