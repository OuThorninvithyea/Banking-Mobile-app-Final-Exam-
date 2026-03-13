package com.hustle.bankapp.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.Account
import com.hustle.bankapp.data.AccountType
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.GlassCard
import com.hustle.bankapp.ui.components.glassmorphism
import com.hustle.bankapp.ui.dashboard.TransactionItem
import com.hustle.bankapp.ui.dashboard.formatAsCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var deletingAccount by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Accounts", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = BackgroundBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BinanceGreen,
                contentColor = BackgroundBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is AccountsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BinanceGreen)
                }
                is AccountsUiState.Error -> {
                    Text(text = state.message, color = ErrorRed, modifier = Modifier.align(Alignment.Center))
                }
                is AccountsUiState.Success -> {
                    AccountsContent(
                        state = state,
                        onEditAccount = { editingAccount = it },
                        onDeleteAccount = { deletingAccount = it }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type ->
                viewModel.createAccount(name, type)
                showAddDialog = false
            }
        )
    }

    editingAccount?.let { account ->
        EditAccountDialog(
            account = account,
            onDismiss = { editingAccount = null },
            onConfirm = { name, type ->
                viewModel.editAccount(account.id, name, type)
                editingAccount = null
            }
        )
    }

    deletingAccount?.let { account ->
        AlertDialog(
            onDismissRequest = { deletingAccount = null },
            containerColor = SurfaceDark,
            title = { Text("Delete Account", color = ErrorRed) },
            text = {
                Column {
                    Text("Are you sure you want to delete \"${account.accountName}\"?", color = TextSecondary)
                    if (account.balance > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This account has ${account.balance.formatAsCurrency()}. Please transfer the balance first.",
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(account.id)
                    deletingAccount = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { deletingAccount = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun AccountsContent(
    state: AccountsUiState.Success,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TotalBalanceCard(state.totalBalance)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label = "Spent",
                    value = state.monthlySpent.formatAsCurrency(),
                    icon = Icons.Filled.ArrowUpward,
                    tint = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Received",
                    value = state.monthlyReceived.formatAsCurrency(),
                    icon = Icons.Filled.ArrowDownward,
                    tint = BinanceGreen,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Transactions",
                    value = state.transactionCount.toString(),
                    icon = Icons.Filled.Receipt,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.spendingByCategory.isNotEmpty()) {
            item {
                SpendingBreakdownCard(state.spendingByCategory)
            }
        }

        item {
            Text(
                text = "Your Accounts",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(state.accounts) { account ->
            val isPrimary = account.accountName == "Primary Account"
            AccountItem(
                account = account,
                showActions = !isPrimary,
                onEdit = { onEditAccount(account) },
                onDelete = { onDeleteAccount(account) }
            )
        }

        if (state.recentTransactions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Recent Transactions",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(state.recentTransactions) { transaction ->
                TransactionItem(transaction)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun TotalBalanceCard(balance: Double) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Total Balance", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = balance.formatAsCurrency(),
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AccountItem(
    account: Account,
    showActions: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = BinanceGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = account.accountName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = account.accountNumber, color = TextSecondary, fontSize = 12.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = account.balance.formatAsCurrency(),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = account.type.name.lowercase().capitalize(),
                        color = BinanceGreen,
                        fontSize = 12.sp
                    )
                }
            }

            if (showActions) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = BinanceGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", color = BinanceGreen, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete", color = ErrorRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.glassmorphism(cornerRadius = 16.dp, alpha = 0.4f)) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SpendingBreakdownCard(categories: List<SpendingCategory>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Spending Breakdown",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            categories.forEach { category ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            category.name,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            category.amount.formatAsCurrency(),
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = RobotoMono
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = category.percentage.coerceIn(0.02f, 1f))
                                .clip(RoundedCornerShape(3.dp))
                                .background(BinanceGreen)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${(category.percentage * 100).toInt()}%",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(account.accountName) }
    var selectedType by remember { mutableStateOf(account.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Edit Account", color = TextPrimary) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Account Type", color = TextSecondary, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    AccountType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.lowercase().capitalize()) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BinanceGreen,
                                selectedLabelColor = BackgroundBlack
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newName = if (name != account.accountName) name else null
                val newType = if (selectedType != account.type) selectedType.name else null
                onConfirm(newName, newType)
            }) { Text("Save", color = BinanceGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, AccountType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.CURRENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("New Account", color = TextPrimary) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Account Type", color = TextSecondary, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    AccountType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.lowercase().capitalize()) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BinanceGreen,
                                selectedLabelColor = BackgroundBlack
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, selectedType) }) {
                Text("Create", color = BinanceGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
