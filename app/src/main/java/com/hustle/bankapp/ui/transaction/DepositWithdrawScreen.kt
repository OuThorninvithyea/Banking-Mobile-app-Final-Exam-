package com.hustle.bankapp.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositWithdrawScreen(
    viewModel: DepositWithdrawViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // The accent color is determined by the transaction type
    val isDeposit = viewModel.transactionType == TransactionType.DEPOSIT
    val accentColor = if (isDeposit) BinanceGreen else ErrorRed
    val screenTitle = if (isDeposit) "Deposit" else "Withdraw"

    // Success animation state
    var showSuccess by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccess = true
            delay(1500) // Let user see the animation
            viewModel.resetSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, color = accentColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlack)
            )
        }
    ) { innerPadding ->
        // Success Overlay
        if (showSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isDeposit) "↑" else "↓",
                                color = accentColor,
                                fontSize = 48.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isDeposit) "Deposited!" else "Withdrawn!",
                            color = accentColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${"%.2f".format(uiState.amountAsDouble)}",
                            color = TextPrimary,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error message
                if (uiState.error != null) {
                    Surface(
                        color = ErrorRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount display — big and centered with accent color
                Text(
                    text = "$${if (uiState.amountString.isEmpty()) "0.00" else uiState.amountString}",
                    color = if (uiState.amountString.isEmpty()) TextSecondary else accentColor,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Custom number pad (reused pattern from Phase 4)
                TransactionNumberPad(
                    onNumberClick = { viewModel.updateAmount(it.toString()) },
                    onDecimalClick = { viewModel.updateAmount(".") },
                    onDeleteClick = { viewModel.updateAmount("DELETE") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit button styled with accent color
                Button(
                    onClick = { viewModel.submit() },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (isDeposit) BackgroundBlack else TextPrimary,
                        disabledContainerColor = SurfaceDark,
                        disabledContentColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = accentColor, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Confirm $screenTitle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TransactionNumberPad(
    onNumberClick: (Int) -> Unit,
    onDecimalClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    val isAction = key == "." || key == "DEL"
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isAction) SurfaceDark else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable {
                                when (key) {
                                    "." -> onDecimalClick()
                                    "DEL" -> onDeleteClick()
                                    else -> onNumberClick(key.toInt())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            color = if (key == "DEL") ErrorRed else TextPrimary,
                            fontSize = if (isAction) 20.sp else 28.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = RobotoMono
                        )
                    }
                }
            }
        }
    }
}
