package com.hustle.bankapp.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.GlassCard
import com.hustle.bankapp.ui.components.glassmorphism
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class NotificationData(
    val title: String,
    val subtitle: String,
    val time: String,
    val isRead: Boolean = false
)

private fun sampleNotifications() = listOf(
    NotificationData("Transfer Received", "You received \$250.00 from Mom", "2 min ago"),
    NotificationData("Card Frozen", "Your virtual card ending 4821 has been frozen", "15 min ago"),
    NotificationData("Deposit Successful", "Your deposit of \$500.00 was processed", "1 hr ago"),
    NotificationData("Security Alert", "New login detected from Android device", "3 hrs ago"),
    NotificationData("Promo", "Get 2% cashback on all transfers this week!", "5 hrs ago", isRead = true),
    NotificationData("Account Created", "Your Savings account is now active", "1 day ago", isRead = true)
)

fun Double.formatAsCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(this)
}

fun String.formatAsDate(): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US)
        val date = parser.parse(this) ?: Date()
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        this
    }
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
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToReceive: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState(initial = DashboardUiState.Loading)
    var isBalanceVisible by remember { mutableStateOf(false) }

    // Refresh data every time this screen becomes visible
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    DashboardContent(
                        state = state,
                        visible = visible,
                        isBalanceVisibleInit = isBalanceVisible,
                        onBalanceToggle = { isBalanceVisible = it },
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToQrScanner = onNavigateToQrScanner,
                        onNavigateToDeposit = onNavigateToDeposit,
                        onNavigateToTransfer = onNavigateToTransfer,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToWithdraw = onNavigateToWithdraw,
                        onNavigateToCards = onNavigateToCards,
                        onNavigateToAccounts = onNavigateToAccounts,
                        onNavigateToFavorites = onNavigateToFavorites,
                        onNavigateToReceive = onNavigateToReceive
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardUiState.Success,
    visible: Boolean,
    isBalanceVisibleInit: Boolean,
    onBalanceToggle: (Boolean) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateToCards: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToReceive: () -> Unit = {}
) {
    var showNotifications by remember { mutableStateOf(false) }
    val notifications = remember { mutableStateListOf(*sampleNotifications().toTypedArray()) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        // 1. HEADER (Top Bar)
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -20 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile + Greeting
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToProfile() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, "Profile", tint = BinanceGreen, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Good afternoon!", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = state.userName.ifEmpty { "User" },
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Utility Icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.MailOutline, "Messages", tint = TextPrimary, modifier = Modifier.size(22.dp))
                        Box {
                            Icon(
                                Icons.Filled.NotificationsNone,
                                "Alerts",
                                tint = TextPrimary,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { showNotifications = true }
                            )
                            val unreadCount = notifications.count { !it.isRead }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unreadCount",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showNotifications,
                                onDismissRequest = { showNotifications = false },
                                modifier = Modifier
                                    .width(320.dp)
                                    .background(SurfaceDark),
                                offset = DpOffset(0.dp, 4.dp)
                            ) {
                                Text(
                                    "Notifications",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                notifications.forEachIndexed { index, notif ->
                                    DropdownMenuItem(
                                        onClick = {
                                            notifications[index] = notif.copy(isRead = true)
                                            showNotifications = false
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.Top,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 6.dp)
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (notif.isRead) TextSecondary.copy(alpha = 0.3f)
                                                            else BinanceGreen
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        notif.title,
                                                        color = if (notif.isRead) TextSecondary else TextPrimary,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        notif.subtitle,
                                                        color = TextSecondary,
                                                        fontSize = 12.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    notif.time,
                                                    color = TextSecondary.copy(alpha = 0.7f),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    )
                                    if (index < notifications.lastIndex) {
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                    }
                                }
                            }
                        }
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            "QR Code",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp).clickable { onNavigateToQrScanner() }
                        )
                    }
                }
            }
        }

        // 2. PRIMARY BALANCE CARD (Hero)
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { 20 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    BinanceGreen.copy(alpha = 0.35f),
                                    SurfaceDark.copy(alpha = 0.95f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Top Row: USD label + eye toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "USD",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // "Default" badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BinanceGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Default",
                                        color = BinanceGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onBalanceToggle(!isBalanceVisibleInit) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBalanceVisibleInit) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle Balance",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Balance + Mascot row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedContent(
                                targetState = isBalanceVisibleInit,
                                transitionSpec = {
                                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 })
                                        .togetherWith(fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 2 })
                                },
                                label = "balance_toggle"
                            ) { balanceVisible ->
                                Text(
                                    text = if (balanceVisible) state.balance.formatAsCurrency() else "•••• ••••",
                                    color = TextPrimary,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = RobotoMono,
                                    letterSpacing = (-1).sp,
                                    modifier = if (balanceVisible) Modifier else Modifier.blur(14.dp)
                                )
                            }
                            // Mascot placeholder
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = "Bank",
                                tint = BinanceGreen.copy(alpha = 0.5f),
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HeroActionButton(
                                icon = Icons.Filled.ArrowDownward,
                                label = "Receive",
                                onClick = onNavigateToReceive,
                                modifier = Modifier.weight(1f)
                            )
                            HeroActionButton(
                                icon = Icons.Filled.ArrowUpward,
                                label = "Send",
                                onClick = onNavigateToTransfer,
                                modifier = Modifier.weight(1f)
                            )
                            HeroActionButton(
                                icon = Icons.Filled.BarChart,
                                label = "Analytics",
                                onClick = onNavigateToHistory,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 3. QUICK SERVICES GRID (3 columns x 2 rows)
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 40 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val services = listOf(
                        ServiceItem("Cards", Icons.Filled.CreditCard, Color(0xFFE74C6F)),
                        ServiceItem("Accounts", Icons.Filled.AccountBalanceWallet, Color(0xFFF5A623)),
                        ServiceItem("Payments", Icons.Filled.Payment, Color(0xFFF5A623)),
                        ServiceItem("Pay", Icons.Filled.QrCodeScanner, Color(0xFFE74C6F)),
                        ServiceItem("Favorites", Icons.Filled.Star, Color(0xFFF5A623)),
                        ServiceItem("Transfers", Icons.Filled.SwapHoriz, Color(0xFFFF7043))
                    )

                    // 2 rows of 3
                    for (rowIndex in 0 until 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            for (colIndex in 0 until 3) {
                                val itemIndex = rowIndex * 3 + colIndex
                                val service = services[itemIndex]
                                ServiceGridCard(
                                    service = service,
                                    modifier = Modifier.weight(1f),
                                    onClick = when (service.label) {
                                        "Cards" -> onNavigateToCards
                                        "Accounts" -> onNavigateToAccounts
                                        "Payments" -> onNavigateToWithdraw
                                        "Pay" -> onNavigateToQrScanner
                                        "Favorites" -> onNavigateToFavorites
                                        "Transfers" -> onNavigateToTransfer
                                        else -> ({})
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. NAVIGATION CHIPS
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 300))
            ) {
                val chips = listOf("Mini Apps", "Transfer", "Rewards", "Promotions", "Lifestyle", "Insurance")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chips) { chip ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDark.copy(alpha = 0.6f))
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = chip,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 5. NEWS & PROMOTIONS BANNER
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 400))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "News & Promotions",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Promo banner card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        BinanceGreen.copy(alpha = 0.3f),
                                        Color(0xFF1A3A2A)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Home Loan",
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Special rate from 6.5% p.a.",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BinanceGreen)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Learn More",
                                        color = BackgroundBlack,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home Loan",
                                tint = BinanceGreen.copy(alpha = 0.5f),
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Supporting Composables ---

@Composable
private fun HeroActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class ServiceItem(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun ServiceGridCard(
    service: ServiceItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Make it square-ish
            .glassmorphism(cornerRadius = 18.dp, alpha = 0.45f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(service.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.label,
                    tint = service.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = service.label,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
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

@Preview(showBackground = true, backgroundColor = 0xFF0B0E11)
@Composable
fun DashboardPreview() {
    val mockState = DashboardUiState.Success(
        balance = 12500.75,
        recentTransactions = listOf(
            Transaction("1", TransactionType.WITHDRAW, 50.0, "2024-03-13T10:00:00.000000Z"),
            Transaction("2", TransactionType.DEPOSIT, 1200.0, "2024-03-12T15:30:00.000000Z")
        ),
        chartData = listOf(11000f, 11500f, 12000f, 11800f, 12500f, 12200f, 12500f),
        userName = "Thorninvithyea"
    )

    MaterialTheme {
        Scaffold(
            containerColor = BackgroundBlack
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                DashboardContent(
                    state = mockState,
                    visible = true,
                    isBalanceVisibleInit = true,
                    onBalanceToggle = {},
                    onNavigateToProfile = {},
                    onNavigateToQrScanner = {},
                    onNavigateToDeposit = {},
                    onNavigateToTransfer = {},
                    onNavigateToHistory = {},
                    onNavigateToWithdraw = {},
                    onNavigateToAccounts = {},
                    onNavigateToFavorites = {},
                    onNavigateToReceive = {}
                )
            }
        }
    }
}
