package com.hustle.bankapp.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.Account
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.OutlinedInputField
import com.hustle.bankapp.ui.components.glassmorphism
import com.hustle.bankapp.ui.dashboard.formatAsCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferAmountScreen(
    viewModel: TransferViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAccountPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccess()
            onNavigateBack()
        }
    }

    if (showAccountPicker) {
        ModalBottomSheet(
            onDismissRequest = { showAccountPicker = false },
            containerColor = SurfaceDark,
            tonalElevation = 0.dp
        ) {
            AccountPickerSheet(
                accounts = uiState.availableAccounts,
                selectedAccount = uiState.selectedSourceAccount,
                onAccountSelected = { account ->
                    viewModel.selectSourceAccount(account)
                    showAccountPicker = false
                }
            )
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

            // Source account selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(cornerRadius = 20.dp, alpha = 0.3f)
                    .clickable { showAccountPicker = true }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = BinanceGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "From: ${uiState.selectedSourceAccount?.accountName ?: "Select Account"}",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Balance: ${uiState.selectedSourceAccount?.balance?.formatAsCurrency() ?: "$0.00"}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select account",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recipient field
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

@Composable
private fun AccountPickerSheet(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Select Source Account",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        HorizontalDivider(color = SurfaceDark, thickness = 1.dp)
        if (accounts.isEmpty()) {
            Text(
                text = "No accounts found",
                color = TextSecondary,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            LazyColumn {
                items(accounts) { account ->
                    val isSelected = account.id == selectedAccount?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(account) }
                            .background(if (isSelected) BinanceGreen.copy(alpha = 0.08f) else Color.Transparent)
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (isSelected) BinanceGreen else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.accountName,
                                color = if (isSelected) BinanceGreen else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                            Text(
                                text = account.accountNumber,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = account.balance.formatAsCurrency(),
                            color = if (isSelected) BinanceGreen else TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                    HorizontalDivider(
                        color = SurfaceDark.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}
