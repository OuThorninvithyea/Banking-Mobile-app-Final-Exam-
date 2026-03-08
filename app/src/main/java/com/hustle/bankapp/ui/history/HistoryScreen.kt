package com.hustle.bankapp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.glassmorphism
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlack)
            )
        }
    ) { innerPadding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions yet.", color = TextSecondary, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionCard(tx)
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: Transaction) {
    val isIncoming = tx.type == TransactionType.DEPOSIT
    val amountColor = if (isIncoming) BinanceGreen else ErrorRed
    val operator = if (isIncoming) "+" else "-"
    val label = tx.type.name.lowercase().replaceFirstChar { it.uppercase() }
    
    val icon = when (tx.type) {
        TransactionType.DEPOSIT -> Icons.Filled.AccountBalanceWallet
        TransactionType.WITHDRAW -> Icons.Filled.ShoppingCart
        TransactionType.TRANSFER -> Icons.Filled.SwapHoriz
    }
    
    val dateStr = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US)
        val date = parser.parse(tx.timestamp) ?: Date()
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(date)
    } catch (e: Exception) {
        tx.timestamp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism(cornerRadius = 16.dp, alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = amountColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateStr,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "$operator${"$%.2f".format(tx.amount)}",
                color = TextPrimary,
                fontFamily = RobotoMono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
