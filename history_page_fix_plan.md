# Fix Plan: History Page Crash

## Problem Description
The Android application crashes when trying to open the **History** page. 

**Root Cause:** There is a JSON data type mismatch for the `timestamp` field in the Transaction object between the Go backend and the Kotlin frontend.
1. The Go backend (`backend/internal/models/transaction.go`) defines `Timestamp time.Time`, which serializes into an ISO-8601 string (e.g., `"2026-03-08T11:00:00Z"`).
2. The Android frontend (`app/src/main/java/com/hustle/bankapp/data/Transaction.kt`) expects `val timestamp: Long`.
3. When Retrofit/Gson tries to parse the string into a `Long`, it throws a `JsonSyntaxException`, which crashes the active Coroutine Flow and prevents the UI from loading.

## User Review Required
None. This is a standard bug fix.

## Proposed Changes

We will fix this on the Android Client side, as it's best practice for APIs to send ISO strings and clients to handle the parsing.

### Android Fontend
#### [MODIFY] `Transaction.kt`(file:///Users/outhorninvuth/Documents/GitHub/Banking-Mobile-app-Final-Exam-/app/src/main/java/com/hustle/bankapp/data/Transaction.kt)
- Change `val timestamp: Long` to `val timestamp: String`.

#### [MODIFY] `HistoryScreen.kt`(file:///Users/outhorninvuth/Documents/GitHub/Banking-Mobile-app-Final-Exam-/app/src/main/java/com/hustle/bankapp/ui/history/HistoryScreen.kt)
- Update the date parsing logic inside the `TransactionCard` composable.
- Instead of treating the timestamp as a `Long` epoch, parse the ISO ISO-8601 string back into a `Date` object before formatting it for the UI.

**Detailed code change for `HistoryScreen.kt`:**
```kotlin
// Replace this:
// val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(tx.timestamp))

// With this:
val dateStr = try {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US) // Or whatever the exact Go format is
    val date = parser.parse(tx.timestamp) ?: Date()
    SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(date)
} catch (e: Exception) {
    // Fallback if parsing fails
    tx.timestamp 
}
```

## Verification Plan

### Automated Tests
- None strictly required. We can verify by compiling the Android app.

### Manual Verification
1. Run the Go backend server or docker-compose setup.
2. Build and run the Android app in the Emulator or a physical device.
3. Log in to an account that has existing transactions (or create a new transaction).
4. Navigate to the History page via the Bottom Navigation Bar.
5. Verify the screen loads successfully without crashing and displays the formatted dates correctly.
