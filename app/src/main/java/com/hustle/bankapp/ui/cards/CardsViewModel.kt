package com.hustle.bankapp.ui.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hustle.bankapp.data.Account
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CardsUiState(
    val cards: List<Card> = emptyList(),
    val selectedCardIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableAccounts: List<Account> = emptyList()
)

class CardsViewModel(private val repository: BankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init {
        loadCards()
        loadAccounts()
    }

    fun loadCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.getCards().collect { cards ->
                    _uiState.value = _uiState.value.copy(
                        cards = cards,
                        isLoading = false,
                        selectedCardIndex = _uiState.value.selectedCardIndex.coerceAtMost(maxOf(0, cards.size - 1))
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            try {
                val profile = repository.getUserProfile().catch { }.first()
                val primaryAccount = if (profile != null) {
                    Account(
                        id = profile.id,
                        accountNumber = profile.accountNumber,
                        accountName = "Primary Account",
                        balance = try { repository.getBalance().catch { }.first() } catch (_: Exception) { 0.0 },
                        type = com.hustle.bankapp.data.AccountType.CURRENT,
                        currency = "USD"
                    )
                } else null

                val createdAccounts = try {
                    repository.getAccounts().catch { }.first()
                } catch (_: Exception) {
                    emptyList()
                }

                val all = listOfNotNull(primaryAccount) + createdAccounts
                _uiState.value = _uiState.value.copy(availableAccounts = all)
            } catch (_: Exception) { }
        }
    }

    fun selectCard(index: Int) {
        _uiState.value = _uiState.value.copy(selectedCardIndex = index)
    }

    fun createCard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.createCard().fold(
                onSuccess = { loadCards() },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.localizedMessage) }
            )
        }
    }

    fun toggleFreeze(cardId: String) {
        viewModelScope.launch {
            repository.toggleFreezeCard(cardId).fold(
                onSuccess = { updated ->
                    _uiState.value = _uiState.value.copy(
                        cards = _uiState.value.cards.map { if (it.id == updated.id) updated else it }
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.localizedMessage) }
            )
        }
    }

    fun updateLimit(cardId: String, limit: Double) {
        viewModelScope.launch {
            repository.updateCardLimit(cardId, limit).fold(
                onSuccess = { updated ->
                    _uiState.value = _uiState.value.copy(
                        cards = _uiState.value.cards.map { if (it.id == updated.id) updated else it }
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.localizedMessage) }
            )
        }
    }

    fun updateCardInfo(cardId: String, type: String) {
        viewModelScope.launch {
            repository.updateCardInfo(cardId, type).fold(
                onSuccess = { updated ->
                    _uiState.value = _uiState.value.copy(
                        cards = _uiState.value.cards.map { if (it.id == updated.id) updated else it }
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.localizedMessage) }
            )
        }
    }

    fun linkCardToAccount(cardId: String, accountId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.linkCardToAccount(cardId, accountId).fold(
                onSuccess = { updated ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cards = _uiState.value.cards.map { if (it.id == updated.id) updated else it }
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.localizedMessage)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
