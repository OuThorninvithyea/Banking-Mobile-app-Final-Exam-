# Fix Plan: Auto-Login Feature

## Problem Description
Currently, when the user opens the application, they are always forced to log in, even if they have successfully logged in during a previous session. The goal is to keep the user logged in seamlessly.

**Root Cause/Missing Logic:** The `TokenManager.kt` correctly saves the user's JWT token to `SharedPreferences` when they log in. However, `MainActivity.kt` hardcodes the `startDestination` of the Jetpack Navigation graph to `"login"`. It does not check if a valid token already exists on startup.

## User Review Required
None. This is a standard feature enhancement.

## Proposed Changes

We need to check if the user already has a saved token when the app launches. If they do, we change the first screen they see from `"login"` to `"dashboard"`.

### Android Frontend
#### [MODIFY] `MainActivity.kt`(file:///Users/outhorninvuth/Documents/GitHub/Banking-Mobile-app-Final-Exam-/app/src/main/java/com/hustle/bankapp/MainActivity.kt)
1. In `onCreate`, right before setting the `setContent` block, use `tokenManager` to check if a token exists:
```kotlin
val tokenManager = TokenManager(applicationContext)
val hasToken = !tokenManager.getToken().isNullOrEmpty()
```
2. Inside the `NavHost` composable within `setContent`, dynamically set the `startDestination` based on whether the token exists:
```kotlin
NavHost(
    navController = navController,
    startDestination = if (hasToken) "dashboard" else "login"
) {
   // ... rest of the navigation graph
}
```

## Verification Plan

### Automated Tests
- None strictly required. We can verify by compiling the Android app.

### Manual Verification
1. Launch the Android app and log into an account.
2. Verify you reach the Dashboard.
3. Completely close/kill the Android app from the device's recent apps menu.
4. Re-open the app.
5. **Expected Result:** The app should bypass the Login screen and immediately land on the Dashboard.
6. Click the "Logout" button on the Profile screen, verify you are taken to the Login screen.
7. Kill the app and re-open it.
8. **Expected Result:** The app should now start on the Login screen since the token was cleared.
