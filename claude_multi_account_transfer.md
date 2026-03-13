# Multi-Account Transfer Implementation Guide

Apply these changes to support selecting a "Source Account" for transfers.

## 1. Data Layer Selection

### BankApiService.kt
Update the `TransferRequest` and `WithdrawRequest` / `DepositRequest` if your API expects specific account IDs.

```kotlin
data class TransferRequest(
    val amount: Double,
    @SerializedName("recipient_id") val recipientId: String,
    @SerializedName("sender_account_id") val senderAccountId: String // Add this
)
```

### BankRepository.kt
Update the interface methods:

```kotlin
suspend fun processTransfer(amount: Double, recipientId: String, senderAccountId: String): Result<Unit>
```

## 2. UI State & ViewModel

### TransferUiState.kt
```kotlin
data class TransferUiState(
    ...
    val availableAccounts: List<Account> = emptyList(),
    val selectedSourceAccount: Account? = null
)
```

### TransferViewModel.kt
Fetch accounts in `init` and store them in `availableAccounts`.

```kotlin
// In submitTransfer()
val senderId = currentState.selectedSourceAccount?.id ?: ""
repository.processTransfer(amount, recipientId, senderId)
```

## 3. UI Component (TransferAmountScreen.kt)

Add a "Source Account" selector before the Recipient input.

```kotlin
// Example Source Selector Card
Box(
    modifier = Modifier
        .fillMaxWidth()
        .glassmorphism(cornerRadius = 20.dp)
        .clickable { /* Show Account Picker BottomSheet */ }
        .padding(16.dp)
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BinanceGreen)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("From: ${uiState.selectedSourceAccount?.accountName ?: "Select Account"}", color = TextPrimary)
            Text("Balance: $${uiState.selectedSourceAccount?.balance ?: "0.00"}", color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
    }
}
```
