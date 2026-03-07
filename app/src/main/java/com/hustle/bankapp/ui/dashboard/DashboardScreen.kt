package com.hustle.bankapp.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.formatAsCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(this)
}

fun Long.formatAsDate(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTransfer: () -> Unit = {},
    onNavigateToDeposit: () -> Unit = {},
    onNavigateToWithdraw: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToQrScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState(initial = DashboardUiState.Loading)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HustleBank",
                        color = BinanceGreen,
                        fontWeight = FontWeight.Black,
                        fontFamily = RobotoMono,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToQrScanner,
                        modifier = Modifier.testTag("scan_qr_button")
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, "Scan QR", tint = BinanceGreen)
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Filled.History, "History", tint = TextSecondary)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.Person, "Profile", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlack)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BinanceGreen
                    )
                }
                is DashboardUiState.Error -> {
                    Text(
                        text = state.message,
                        color = ErrorRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DashboardUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        BalanceCard(balance = state.balance)
                        Spacer(modifier = Modifier.height(24.dp))
                        ActionButtonsRow(
                            onDepositClick = onNavigateToDeposit,
                            onWithdrawClick = onNavigateToWithdraw,
                            onTransferClick = onNavigateToTransfer
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        TrendChartSection(chartData = state.chartData)
                        Spacer(modifier = Modifier.height(24.dp))
                        RecentTransactionsList(transactions = state.recentTransactions)
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceDark,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = balance.formatAsCurrency(),
                color = TextPrimary,
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}

@Composable
fun ActionButtonsRow(
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onTransferClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            text = "Deposit",
            backgroundColor = BinanceGreen,
            contentColor = BackgroundBlack,
            onClick = onDepositClick
        )
        ActionButton(
            text = "Withdraw",
            backgroundColor = ErrorRed,
            contentColor = TextPrimary,
            onClick = onWithdrawClick
        )
        OutlinedActionButton(
            text = "Transfer",
            onClick = onTransferClick
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextPrimary),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TrendChartSection(chartData: List<Float>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "7-Day Trend",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            color = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        ) {
            CustomLineChart(data = chartData)
        }
    }
}

@Composable
fun CustomLineChart(data: List<Float>) {
    if (data.isEmpty()) return
    val maxVal = data.maxOrNull() ?: return
    val minVal = data.minOrNull() ?: return
    val range = if (maxVal == minVal) 1f else maxVal - minVal

    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        
        val width = size.width
        val height = size.height
        val pointSpacing = width / (data.size - 1).coerceAtLeast(1)

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * pointSpacing
                val normalizedY = 1f - ((value - minVal) / range)
                val y = (normalizedY * height * 0.8f) + (height * 0.1f)

                if (index == 0) {
                    moveTo(x, y)
                } else {
                    val prevX = (index - 1) * pointSpacing
                    val prevNormalizedY = 1f - ((data[index - 1] - minVal) / range)
                    val prevY = (prevNormalizedY * height * 0.8f) + (height * 0.1f)

                    val controlPoint1 = android.graphics.PointF(prevX + pointSpacing / 2f, prevY)
                    val controlPoint2 = android.graphics.PointF(prevX + pointSpacing / 2f, y)
                    cubicTo(
                        controlPoint1.x, controlPoint1.y,
                        controlPoint2.x, controlPoint2.y,
                        x, y
                    )
                }
            }
        }
        
        drawPath(
            path = path,
            color = BinanceGreen,
            style = Stroke(width = 3.dp.toPx())
        )
        
        val lastX = (data.size - 1) * pointSpacing
        val lastNormalizedY = 1f - ((data.last() - minVal) / range)
        val lastY = (lastNormalizedY * height * 0.8f) + (height * 0.1f)
        
        drawCircle(
            color = BinanceGreen,
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
        drawCircle(
            color = BackgroundBlack,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
    }
}

@Composable
fun RecentTransactionsList(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Recent Transactions",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (transactions.isEmpty()) {
            Text(
                text = "No recent transactions.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isDeduction = transaction.type == TransactionType.WITHDRAW || transaction.type == TransactionType.TRANSFER
    val color = if (isDeduction) ErrorRed else BinanceGreen
    val operator = if (isDeduction) "-" else "+"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.type.name.take(1),
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.timestamp.formatAsDate(),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = "$operator ${transaction.amount.formatAsCurrency()}",
                color = color,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoMono),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
