This occurs because network requests inside the `ProfileViewModel` and `HistoryViewModel` are throwing exceptions (e.g., due to network failures or server errors) that are not being caught. 

Since these requests are Kotlin Flows executed inside Coroutines, an unhandled exception in the flow will crash the application when collected. `DashboardViewModel` already handles this correctly, but `ProfileViewModel` and `HistoryViewModel` do not.

## Proposed Changes

### Fix unhandled Flow exceptions in ViewModels

#### [MODIFY] [ProfileViewModel.kt](file:///Users/vithea/Documents/mobile_banking_app/app/src/main/java/com/hustle/bankapp/ui/profile/ProfileViewModel.kt)
Update the `init` block to catch the `Exception` thrown by the `getUserProfile()` flow. 

```kotlin
    init {
        viewModelScope.launch {
            try {
                repository.getUserProfile().collect { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            editName = user?.name ?: "",
                            editEmail = user?.email ?: "",
                            editContact = user?.contact ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                // Prevent crash and display the error message in the UI state
                _uiState.update { it.copy(error = e.message ?: "Failed to load profile") }
            }
        }
    }
```

#### [MODIFY] [HistoryViewModel.kt](file:///Users/vithea/Documents/mobile_banking_app/app/src/main/java/com/hustle/bankapp/ui/history/HistoryViewModel.kt)
Update the `transactions` flow to catch any exceptions and emit an empty list (or handle the error appropriately). Don't forget to import `catch`.

```kotlin
import kotlinx.coroutines.flow.catch
// ...
class HistoryViewModel(repository: BankRepository) : ViewModel() {
    val transactions: Flow<List<Transaction>> = repository.getTransactions()
        .catch { 
            // Emit an empty list on failure to prevent the app from crashing during collectAsState()
            emit(emptyList()) 
        }
}
```

## Verification Plan

### Manual Verification
1. Launch the app and log in.
2. Disable the device's internet connection (turn on Airplane mode) or make the API server unreachable.
3. Click on the "Profile" (Account) and "History" tabs in the bottom navigation bar.
4. Verify that the app **does not crash** and handles the error gracefully (e.g., UI might show a blank screen or an error message instead of force closing).
