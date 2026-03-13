# INSTRUCTIONS: ENABLE OFFLINE/MOCK TESTING

Apply these changes to allow testing the UI redesign in the emulator without internet connectivity.

## 1. Modify DashboardViewModel.kt
Update `DashboardViewModel` to include a mock data fallback.

**Target File**: `app/src/main/java/com/hustle/bankapp/ui/dashboard/DashboardViewModel.kt`

**Logic to Add**:
Insert a `private val USE_MOCK_DATA = true` flag. Update the `uiState` flow to check this flag:

```kotlin
// In DashboardViewModel
private val USE_MOCK_DATA = true // Set to true to test without internet

val uiState: StateFlow<DashboardUiState> = refreshTrigger
    .flatMapLatest {
        if (USE_MOCK_DATA) {
            flow {
                emit(DashboardUiState.Success(
                    balance = 12500.75,
                    recentTransactions = listOf(/* Add 3-5 mock transactions here */),
                    chartData = listOf(11000f, 11500f, 12000f, 11800f, 12500f),
                    userName = "Thorninvithyea (Offline)"
                ) as DashboardUiState)
            }
        } else {
            combine(...) { ... } // Existing repository logic
        }
    }
```

## 2. Add Preview to DashboardScreen.kt
Add a `@Preview` composable at the bottom of `DashboardScreen.kt` to allow visual verification in the IDE.

**Target File**: `app/src/main/java/com/hustle/bankapp/ui/dashboard/DashboardScreen.kt`

**Code to Add**:
```kotlin
@Preview(showBackground = true, backgroundColor = 0xFF0B0E11) // BackgroundBlack
@Composable
fun DashboardPreview() {
    val mockState = DashboardUiState.Success(
        balance = 5420.50,
        recentTransactions = emptyList(),
        chartData = listOf(10f, 20f, 15f, 30f),
        userName = "Preview User"
    )
    
    // Create a mock view model or just call a stateless version of the screen if possible
    // Alternatively, just render the content of the 'Success' branch directly
}
```

## 3. Switching Back to Cloud
When you are ready to test with the real cloud API:
1. Open `DashboardViewModel.kt`.
2. Change the flag: `private val USE_MOCK_DATA = false`.
3. Rebuild and run. The app will immediately revert to using the live `BankRepository` and internet connection.
