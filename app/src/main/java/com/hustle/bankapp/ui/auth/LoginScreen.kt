package com.hustle.bankapp.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.fragment.app.FragmentActivity
import com.hustle.bankapp.util.BiometricPromptManager
import com.hustle.bankapp.util.BiometricResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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

    // Staggered entrance animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Pulse animation on the logo accent dot
    val dotPulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "dot"
    )

    // Navigate when authenticated
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onNavigateToDashboard()
    }

    // Handle biometric authentication result
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
        // Subtle radial glow from BinanceGreen in the upper area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(BinanceGreen.copy(alpha = 0.08f), BackgroundBlack),
                        center = Offset(0f, 0f),
                        radius = 900f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // — HEADER —
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HUSTLE",
                            color = TextPrimary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp,
                            fontFamily = RobotoMono
                        )
                        Spacer(Modifier.width(6.dp))
                        // Animated accent dot replacing "BANK"
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .alpha(dotPulse)
                                .clip(
                                    RoundedCornerShape(50)
                                )
                                .background(BinanceGreen)
                        )
                        Text(
                            text = "BANK",
                            color = BinanceGreen,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp,
                            fontFamily = RobotoMono
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "SECURE • FAST • RELIABLE",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp,
                        fontFamily = RobotoMono
                    )
                }
            }

            Spacer(Modifier.height(52.dp))

            // — FORM CARD —
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 60 }
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceDark,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Sign In",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter your credentials to access your account",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(28.dp))

                        // Account Number Field
                        OutlinedTextField(
                            value = uiState.accountNumber,
                            onValueChange = viewModel::onAccountNumberChange,
                            label = { Text("Account Number", color = TextSecondary) },
                            placeholder = { Text("e.g. 001-234-5678", color = TextSecondary.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinanceGreen,
                                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = BinanceGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // PIN / Password Field
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = { Text("PIN / Password", color = TextSecondary) },
                            singleLine = true,
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }),
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle visibility",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinanceGreen,
                                unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = BinanceGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Error message
                        AnimatedVisibility(visible = uiState.error != null) {
                            Surface(
                                color = ErrorRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Text(
                                    text = uiState.error ?: "",
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Primary Login Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.login()
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinanceGreen,
                                contentColor = BackgroundBlack
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = BackgroundBlack,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Biometric login button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SurfaceDark,
                                tonalElevation = 2.dp
                            ) {
                                IconButton(
                                    onClick = {
                                        promptManager.showBiometricPrompt(
                                            title = "Biometric Login",
                                            subtitle = "Use your fingerprint or face to sign in"
                                        )
                                    },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .testTag("biometrics_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = "Sign in with biometrics",
                                        tint = BinanceGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "or use biometrics",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        // Secondary Register Link
                        TextButton(
                            onClick = onNavigateToRegister,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "New customer? Create an account",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Hint text for demo
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 400))
            ) {
                Text(
                    text = "Demo  •  Account: 001-234-5678  •  PIN: 1234",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = RobotoMono,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
