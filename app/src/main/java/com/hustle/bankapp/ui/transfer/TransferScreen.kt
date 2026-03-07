package com.hustle.bankapp.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.hustle.bankapp.ui.components.OutlinedInputField
import com.hustle.bankapp.ui.components.glassmorphism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                title = { Text("Transfer Funds", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlack)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (uiState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphism(alpha = 0.2f, borderColor = ErrorRed.copy(alpha = 0.5f))
                        .padding(bottom = 16.dp)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(cornerRadius = 24.dp, alpha = 0.3f)
                    .padding(20.dp)
            ) {
                OutlinedInputField(
                    value = uiState.recipientId,
                    onValueChange = { viewModel.updateRecipientId(it) },
                    label = "Recipient Email or Account ID"
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$${if (uiState.amountString.isEmpty()) "0.00" else uiState.amountString}",
                    color = if (uiState.amountString.isEmpty()) TextSecondary else BinanceGreen,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RobotoMono
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CustomNumberPad(
                onNumberClick = { viewModel.updateAmount(it.toString()) },
                onDecimalClick = { viewModel.updateAmount(".") },
                onDeleteClick = { viewModel.updateAmount("DELETE") }
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = BackgroundBlack, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm Transfer", fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp)
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
            .size(76.dp)
            .clip(CircleShape)
            .background(if (isActionKey) SurfaceDark else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (text == "DEL") ErrorRed else TextPrimary,
            fontSize = if (isActionKey) 22.sp else 32.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = RobotoMono
        )
    }
}
