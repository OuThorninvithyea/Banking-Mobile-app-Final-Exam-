package com.hustle.bankapp.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle success navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = { Text("Transfer Funds", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlack
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Error Message
            if (uiState.error != null) {
                Surface(
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
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

            // Recipient Input
            OutlinedTextField(
                value = uiState.recipientId,
                onValueChange = { viewModel.updateRecipientId(it) },
                label = { Text("Recipient ID", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BinanceGreen,
                    unfocusedBorderColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = BinanceGreen
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Amount Display
            Text(
                text = "$${if (uiState.amountString.isEmpty()) "0.00" else uiState.amountString}",
                color = if (uiState.amountString.isEmpty()) TextSecondary else BinanceGreen,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Custom Number Pad
            CustomNumberPad(
                onNumberClick = { viewModel.updateAmount(it.toString()) },
                onDecimalClick = { viewModel.updateAmount(".") },
                onDeleteClick = { viewModel.updateAmount("DELETE") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { viewModel.submitTransfer() },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BinanceGreen,
                    contentColor = BackgroundBlack,
                    disabledContainerColor = SurfaceDark,
                    disabledContentColor = TextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = BinanceGreen,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Confirm Transfer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CustomNumberPad(
    onNumberClick: (Int) -> Unit,
    onDecimalClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "DEL")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    NumberPadKey(
                        text = key,
                        onClick = {
                            when (key) {
                                "." -> onDecimalClick()
                                "DEL" -> onDeleteClick()
                                else -> onNumberClick(key.toInt())
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NumberPadKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActionKey = text == "." || text == "DEL"
    
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(if (isActionKey) SurfaceDark else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (text == "DEL") ErrorRed else TextPrimary,
            fontSize = if (isActionKey) 20.sp else 28.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = RobotoMono
        )
    }
}
