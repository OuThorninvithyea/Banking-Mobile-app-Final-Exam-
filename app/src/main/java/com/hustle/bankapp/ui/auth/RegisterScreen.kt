package com.hustle.bankapp.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.BrandButton
import com.hustle.bankapp.ui.components.GlassCard
import com.hustle.bankapp.ui.components.OutlinedInputField

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var pwVisible by remember { mutableStateOf(false) }
    var cpwVisible by remember { mutableStateOf(false) }
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Navigate on success
    LaunchedEffect(uiState.registeredUser) {
        if (uiState.registeredUser != null) onNavigateToDashboard()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            // Header Text aligned to start, matching the new aesthetic
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "Create Account",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = com.hustle.bankapp.theme.Inter
                    )
                    Text(
                        text = "Join HustleBank today.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, 180)) + slideInVertically(tween(700, 180)) { 60 }
            ) {
                GlassCard {
                    OutlinedInputField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Full Name"
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedInputField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email"
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedInputField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Password",
                        isPassword = !pwVisible,
                        trailingIcon = {
                            IconButton(onClick = { pwVisible = !pwVisible }) {
                                Icon(
                                    imageVector = if (pwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedInputField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = "Confirm Password",
                        isPassword = !cpwVisible,
                        trailingIcon = {
                            IconButton(onClick = { cpwVisible = !cpwVisible }) {
                                Icon(
                                    imageVector = if (cpwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
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
                        text = "Create Account",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.register()
                        },
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

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
                        text = "Already have an account? ",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign In",
                        color = BinanceGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }
            }
        }
    }
}
