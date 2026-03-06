package com.hustle.bankapp.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
                title = { Text("Transaction History", color = TextPrimary) },
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
    val isSentTransfer = tx.type == TransactionType.TRANSFER
    val amountColor = if (isIncoming) BinanceGreen else ErrorRed
    val icon = when (tx.type) {
        TransactionType.DEPOSIT -> Icons.Filled.ArrowDownward
        TransactionType.WITHDRAW -> Icons.Filled.ArrowUpward
        TransactionType.TRANSFER -> Icons.Filled.SwapHoriz
    }
    val label = when (tx.type) {
        TransactionType.DEPOSIT -> "Deposit"
        TransactionType.WITHDRAW -> "Withdrawal"
        TransactionType.TRANSFER -> "Transfer"
    }
    val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(tx.timestamp))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Surface(
                shape = CircleShape,
                color = amountColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = amountColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            // Label + date
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

            // Amount
            Text(
                text = "${if (isIncoming) "+" else "-"}${"$%.2f".format(tx.amount)}",
                color = amountColor,
                fontFamily = RobotoMono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
