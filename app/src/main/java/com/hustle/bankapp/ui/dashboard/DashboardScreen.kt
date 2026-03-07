package com.hustle.bankapp.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.GlassCard
import com.hustle.bankapp.ui.components.glassmorphism
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
    var isBalanceVisible by remember { mutableStateOf(true) }
    
    // Entrance animation trigger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Subtle top-left green glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(BinanceGreen.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(0f, 0f),
                            radius = 800f
                        )
                    )
            )

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
                    // We use LazyColumn as the root scrollable container
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp) // buffer for bottom nav
                    ) {
                        
                        // 1. HEADER
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -20 }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onNavigateToProfile() }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceDark),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Person, "Profile", tint = BinanceGreen, modifier = Modifier.size(28.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("Welcome back,", color = TextSecondary, fontSize = 13.sp)
                                            Text("Alex Johnson", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .glassmorphism(cornerRadius = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.NotificationsNone, "Alerts", tint = TextPrimary)
                                        // Notification red dot indicator
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(ErrorRed)
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-8).dp, y = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. MAIN BALANCE SECTION
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { 20 }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Total Balance",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isBalanceVisible) state.balance.formatAsCurrency() else "*******",
                                            color = TextPrimary,
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = RobotoMono,
                                            letterSpacing = (-1).sp,
                                            modifier = if (isBalanceVisible) Modifier else Modifier.blur(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        IconButton(onClick = { isBalanceVisible = !isBalanceVisible }) {
                                            Icon(
                                                imageVector = if (isBalanceVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                contentDescription = "Toggle Balance",
                                                tint = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. QUICK ACTIONS GRID
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 40 }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    QuickAction(icon = Icons.Filled.ArrowUpward, label = "Send", onClick = onNavigateToTransfer)
                                    QuickAction(icon = Icons.Filled.ArrowDownward, label = "Receive", onClick = onNavigateToDeposit)
                                    QuickAction(icon = Icons.Filled.Payment, label = "Pay", onClick = onNavigateToWithdraw)
                                    QuickAction(icon = Icons.Filled.MoreHoriz, label = "More", onClick = onNavigateToHistory)
                                }
                            }
                        }

                        // 4. OVERVIEW CARDS (Income vs Spent)
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(800, delayMillis = 300)) + slideInVertically(tween(800, delayMillis = 300)) { 60 }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Mock calculation for UI purposes based on transactions
                                    val income = state.recentTransactions.filter { it.type == TransactionType.DEPOSIT }.sumOf { it.amount }
                                    val spent = state.recentTransactions.filter { it.type == TransactionType.WITHDRAW || it.type == TransactionType.TRANSFER }.sumOf { it.amount }

                                    OverviewCard(
                                        title = "Income",
                                        amount = income,
                                        iconColor = BinanceGreen,
                                        isPositive = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OverviewCard(
                                        title = "Spent",
                                        amount = spent,
                                        iconColor = ErrorRed,
                                        isPositive = false,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // 5. TREND CHART
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(800, delayMillis = 400))
                            ) {
                                GlassCard(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                                    Text(
                                        text = "Activity Trend",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                    ) {
                                        CustomLineChart(data = state.chartData)
                                    }
                                }
                            }
                        }

                        // 6. TRANSACTIONS HEADER
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(900, delayMillis = 500))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Recent Activity", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    Text("View All", color = BinanceGreen, fontSize = 14.sp, modifier = Modifier.clickable { onNavigateToHistory() })
                                }
                            }
                        }

                        // 7. TRANSACTIONS LIST
                        if (state.recentTransactions.isEmpty()) {
                            item {
                                Text(
                                    text = "No recent transactions.",
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            }
                        } else {
                            items(state.recentTransactions.take(5), key = { it.id }) { transaction ->
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(tween(1000, delayMillis = 600))
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                                        TransactionItem(transaction = transaction)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .glassmorphism(cornerRadius = 20.dp, alpha = 0.4f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BinanceGreen,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OverviewCard(
    title: String,
    amount: Double,
    iconColor: Color,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.glassmorphism(cornerRadius = 20.dp, alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = amount.formatAsCurrency(),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoMono
            )
        }
    }
}

@Composable
fun CustomLineChart(data: List<Float>) {
    if (data.isEmpty()) return
    val maxVal = data.maxOrNull() ?: return
    val minVal = data.minOrNull() ?: return
    val range = if (maxVal == minVal) 1f else maxVal - minVal

    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
        val width = size.width
        val height = size.height
        val pointSpacing = width / (data.size - 1).coerceAtLeast(1)

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * pointSpacing
                val normalizedY = 1f - ((value - minVal) / range)
                val y = (normalizedY * height * 0.8f) + (height * 0.1f)

                if (index == 0) moveTo(x, y)
                else {
                    val prevX = (index - 1) * pointSpacing
                    val prevNormalizedY = 1f - ((data[index - 1] - minVal) / range)
                    val prevY = (prevNormalizedY * height * 0.8f) + (height * 0.1f)
                    
                    val controlPoint1 = android.graphics.PointF(prevX + pointSpacing / 2f, prevY)
                    val controlPoint2 = android.graphics.PointF(prevX + pointSpacing / 2f, y)
                    cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, x, y)
                }
            }
        }
        
        drawPath(path = path, color = BinanceGreen, style = Stroke(width = 3.dp.toPx()))
        
        // Emphasize the last point
        val lastX = (data.size - 1) * pointSpacing
        val lastNormalizedY = 1f - ((data.last() - minVal) / range)
        val lastY = (lastNormalizedY * height * 0.8f) + (height * 0.1f)
        
        drawCircle(color = BinanceGreen.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = Offset(lastX, lastY))
        drawCircle(color = BinanceGreen, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isDeduction = transaction.type == TransactionType.WITHDRAW || transaction.type == TransactionType.TRANSFER
    val color = if (isDeduction) ErrorRed else BinanceGreen
    val operator = if (isDeduction) "-" else "+"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism(cornerRadius = 16.dp, alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDeduction) Icons.Filled.ShoppingCart else Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = transaction.timestamp.formatAsDate(),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "$operator${transaction.amount.formatAsCurrency()}",
                color = TextPrimary,
                fontSize = 16.sp,
                fontFamily = RobotoMono,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
