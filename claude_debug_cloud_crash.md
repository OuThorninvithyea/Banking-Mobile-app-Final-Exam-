# Debugging Cloud Crash: Transfer Flow

The crash you're seeing in Cloud mode is likely caused by **unhandled network exceptions** when the app tries to fetch your accounts from the server. In Mock mode, these calls never fail, but in Cloud mode, any network issue or missing API endpoint will cause a crash if not caught.

## 1. The Main Culprit: TransferViewModel.kt

The `init` block launches a coroutine to fetch accounts but doesn't handle failures. If the API call fails, the app crashes.

### Fix: Add Error Handling to Init
```kotlin
// TransferViewModel.kt
init {
    viewModelScope.launch {
        repository.getAccounts()
            .catch { e -> 
                _uiState.update { it.copy(error = e.message ?: "Failed to load accounts") }
            }
            .collect { accounts ->
                _uiState.update { state ->
                    state.copy(
                        availableAccounts = accounts,
                        selectedSourceAccount = state.selectedSourceAccount ?: accounts.firstOrNull()
                    )
                }
            }
    }
}
```

## 2. Unprotected Balance Fetch

Inside `submitTransfer()`, calling `.first()` on a flow can also throw an exception if the network fails at that exact moment.

### Fix: Wrap in Try-Catch
```kotlin
// Inside submitTransfer()
viewModelScope.launch {
    try {
        val currentBalance = repository.getBalance().first()
        // ... rest of your logic ...
    } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.message}") }
    }
}
```

## 3. Graceful UI Placeholders (TransferSelectionScreen.kt)

If the favorites fail to load, ensure the screen shows an error message instead of just empty boxes.

### Improvement:
```kotlin
when (state) {
    is TransferSelectionUiState.Error -> {
        Text("Error loading favorites: ${state.message}", color = ErrorRed, modifier = Modifier.padding(16.dp))
    }
    is TransferSelectionUiState.Success -> {
        // ... existing loop ...
    }
    // ...
}
```

## 4. API Model Safety (RemoteBankRepositoryImpl.kt)

Check for "!!” (double-bang) operators. If the server returns a `200 OK` but an empty body, `response.body()!!` will crash the app.

### Fix: Use Safe Access
Change `Result.success(response.body()!!)` to:
```kotlin
val body = response.body()
if (body != null) {
    Result.success(body)
} else {
    Result.failure(Exception("Server returned empty data"))
}
```
