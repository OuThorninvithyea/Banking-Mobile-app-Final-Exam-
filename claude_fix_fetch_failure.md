# Fix: Cloud Fetch Failure & Serialization

This guide fixes the "failed to fetch" issues by ensuring the app and server are perfectly synchronized in their data models and network paths.

## 1. Model Alignment (Data Layer)

### Transaction.kt
The backend uses `recipient_id`. We need to tell GSON how to map it.

```kotlin
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val timestamp: String,
    @SerializedName("recipient_id") val recipientId: String? = null,
    val category: String = "General"
)
```

### User.kt
Ensure all fields are safe for the backend.

```kotlin
data class User(
    val id: String,
    val name: String,
    @SerializedName("account_number") val accountNumber: String,
    val balance: Double,
    val email: String = "",
    val contact: String = ""
)
```

## 2. API Path Standardization

### BankApiService.kt
Retrofit works best when relative paths **do not** start with a `/` if the Base URL ends with one.

```kotlin
// Change from:
@GET("/api/user/balance")
// To:
@GET("api/user/balance")
```
**Apply this to ALL endpoints in `BankApiService.kt`.**

## 3. Better Error Reporting

### RemoteBankRepositoryImpl.kt
Update the `Flow` catch blocks and `isSuccessful` checks to show the actual server error.

```kotlin
// Example for getBalance()
if (response.isSuccessful) {
    emit(response.body()?.balance ?: 0.0)
} else {
    val errorMsg = response.errorBody()?.string() ?: response.message()
    throw Exception("API Error: $errorMsg")
}
```

## 4. Double Check Dashboard

Ensure `DashboardViewModel.kt` is NOT in `USE_MOCK_DATA = true` mode.

```kotlin
private val USE_MOCK_DATA = false
```
