package com.hustle.bankapp.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class BiometricResult {
    data object Success : BiometricResult()
    data class Error(val message: String) : BiometricResult()
    data object Failed : BiometricResult()
    data object HardwareUnavailable : BiometricResult()
    data object NoBiometricEnrolled : BiometricResult()
}

class BiometricPromptManager(private val activity: FragmentActivity) {

    private val _result = MutableStateFlow<BiometricResult?>(null)
    val result: StateFlow<BiometricResult?> = _result

    fun showBiometricPrompt(
        title: String = "Biometric Login",
        subtitle: String = "Use your fingerprint or face to sign in"
    ) {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                _result.value = BiometricResult.HardwareUnavailable
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                _result.value = BiometricResult.NoBiometricEnrolled
                return
            }
            else -> Unit
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    _result.value = BiometricResult.Success
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    _result.value = BiometricResult.Error(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    _result.value = BiometricResult.Failed
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    fun resetResult() {
        _result.value = null
    }
}
