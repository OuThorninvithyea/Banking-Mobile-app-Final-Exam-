# Task: Implement Biometric Login Prompt

## Context
We already have the Biometric authentication dependencies added to our `build.gradle.kts` and a utility class `BiometricPromptManager` located in `app/src/main/java/com/hustle/bankapp/util/BiometricPromptManager.kt`. 

In our `LoginScreen` (`app/src/main/java/com/hustle/bankapp/ui/auth/LoginScreen.kt`), we have already initialized the `BiometricPromptManager` and set up the `LaunchedEffect` to listen for `BiometricResult.Success` and navigate the user to the dashboard. 

## Action Required
Update `LoginScreen.kt` to provide a visual button that triggers the biometric prompt.

### Specific Instructions for Cursor:
1. **Locate `LoginScreen.kt`**: Open `app/src/main/java/com/hustle/bankapp/ui/auth/LoginScreen.kt`.
2. **Add Biometric Button**: Add a Biometric (Fingerprint/FaceID) icon button to the UI. 
   - A good place for this is right next to the primary "Sign In" button, or just below it.
   - You can use the `Icons.Filled.Fingerprint` icon.
   - When clicked, this button should call `promptManager.showBiometricPrompt()`.
3. **Styling**: Ensure the button matches the existing dark/hacker aesthetic (using `BinanceGreen` or `TextSecondary` colors and matching shapes). Provide an elegant design that feels native to the `HustleBankTheme`.
4. **Visibility Check (Optional but recommended)**: Ideally, only show the biometric button if the device actually supports biometrics. You can assume it is supported for this task, or write a quick check.

*Please write the code to modify `LoginScreen.kt` with this new Biometric Button.*
