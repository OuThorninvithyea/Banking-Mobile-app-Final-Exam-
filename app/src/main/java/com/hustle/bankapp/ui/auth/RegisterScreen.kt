package com.hustle.bankapp.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
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
        // Teal radial glow from the top-right corner for freshness/distinction from Login
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(BinanceGreen.copy(alpha = 0.06f), BackgroundBlack),
                        center = Offset(Float.MAX_VALUE, 0f),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // — Header —
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Account",
                        color = TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Join HustleBank and get a \$500 signup bonus",
                        color = BinanceGreen,
                        fontSize = 13.sp,
                        fontFamily = RobotoMono,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // — Form Card —
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, 180)) + slideInVertically(tween(700, 180)) { 60 }
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Full Name
                        FormField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChange,
                            label = "Full Name",
                            placeholder = "Alex Johnson",
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Email
                        FormField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = "Email",
                            placeholder = "alex@example.com",
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Password
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = { Text("Password", color = TextSecondary) },
                            singleLine = true,
                            visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            trailingIcon = {
                                IconButton(onClick = { pwVisible = !pwVisible }) {
                                    Icon(
                                        imageVector = if (pwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = authFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Confirm Password
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChange,
                            label = { Text("Confirm Password", color = TextSecondary) },
                            singleLine = true,
                            visualTransformation = if (cpwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.register()
                            }),
                            trailingIcon = {
                                IconButton(onClick = { cpwVisible = !cpwVisible }) {
                                    Icon(
                                        imageVector = if (cpwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = authFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Error
                        AnimatedVisibility(visible = uiState.error != null) {
                            Surface(
                                color = ErrorRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
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

                        Spacer(Modifier.height(4.dp))

                        // Create Account button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.register()
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
                                Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            }
                        }

                        // Back to login
                        TextButton(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Already have an account? Sign In",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.4f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = authFieldColors(),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BinanceGreen,
    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = BinanceGreen
)
