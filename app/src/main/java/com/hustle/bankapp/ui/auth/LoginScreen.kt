package com.hustle.bankapp.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.BrandButton
import com.hustle.bankapp.ui.components.GlassCard
import com.hustle.bankapp.ui.components.OutlinedInputField
import com.hustle.bankapp.util.BiometricPromptManager
import com.hustle.bankapp.util.BiometricResult

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var pinVisible by remember { mutableStateOf(false) }

    val activity = LocalContext.current as FragmentActivity
    val promptManager = remember { BiometricPromptManager(activity) }
    val biometricResult by promptManager.result.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onNavigateToDashboard()
    }

    LaunchedEffect(biometricResult) {
        when (biometricResult) {
            is BiometricResult.Success -> {
                promptManager.resetResult()
                onNavigateToDashboard()
            }
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Header Text aligned to start, matching HTML structure
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "Welcome back",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = com.hustle.bankapp.theme.Inter
                    )
                    Text(
                        text = "Sign in to your account",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 60 }
            ) {
                GlassCard {
                    OutlinedInputField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email Address"
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedInputField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Password",
                        isPassword = !pinVisible,
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = TextSecondary
                                )
                            }
                        }
                    )

                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = ErrorRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    BrandButton(
                        text = "Log in",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        isLoading = uiState.isLoading
                    )

                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        text = "Forgot password?",
                        color = BinanceGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Handle forgot password */ }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 300))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Register",
                        color = BinanceGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToRegister)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))

            // Biometric Option (Glassmorphism circular button)
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(900, delayMillis = 400))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            promptManager.showBiometricPrompt()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(SurfaceDark.copy(alpha = 0.5f), shape = MaterialTheme.shapes.extraLarge)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Sign in with biometrics",
                            tint = BinanceGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "or use biometrics",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
