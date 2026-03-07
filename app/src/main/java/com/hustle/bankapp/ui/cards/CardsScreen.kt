package com.hustle.bankapp.ui.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.data.Transaction
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.data.User
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.GlassCard
import com.hustle.bankapp.ui.components.glassmorphism
import com.hustle.bankapp.ui.dashboard.TransactionItem
import com.hustle.bankapp.ui.dashboard.formatAsCurrency

@Composable
fun CardsScreen(
    repository: BankRepository,
    onNavigateBack: () -> Unit = {}
) {
    val balance by repository.getBalance().collectAsState(initial = 0.0)
    val transactions by repository.getTransactions().collectAsState(initial = emptyList())
    val user by repository.getUserProfile().collectAsState(initial = null)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Subtle top-right green glow matching dashboard aesthetic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(BinanceGreen.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(Float.MAX_VALUE, 0f),
                        radius = 900f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── HEADER ─────────────────────────────────────────────────────
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
                        Text(
                            text = "My Cards",
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = RobotoMono
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .glassmorphism(cornerRadius = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Card",
                                tint = BinanceGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // ── PRIMARY CARD ───────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { 40 }
                ) {
                    PrimaryCard(
                        user = user,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // ── CARD DETAILS ROW ───────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 40 }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CardDetailChip(
                            label = "Available",
                            value = balance.formatAsCurrency(),
                            icon = Icons.Filled.AccountBalanceWallet,
                            modifier = Modifier.weight(1f)
                        )
                        CardDetailChip(
                            label = "Card Limit",
                            value = "$10,000.00",
                            icon = Icons.Filled.CreditScore,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── QUICK ACTIONS ──────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 300)) + slideInVertically(tween(800, delayMillis = 300)) { 40 }
                ) {
                    GlassCard(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = "Card Controls",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CardAction(icon = Icons.Filled.AcUnit, label = "Freeze")
                            CardAction(icon = Icons.Filled.Block, label = "Block")
                            CardAction(icon = Icons.Filled.Tune, label = "Limits")
                            CardAction(icon = Icons.Filled.Autorenew, label = "Replace")
                        }
                    }
                }
            }

            // ── RECENT TRANSACTIONS HEADER ─────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(900, delayMillis = 400))
                ) {
                    Text(
                        text = "Card Transactions",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }

            // ── TRANSACTIONS LIST ──────────────────────────────────────────
            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No card transactions yet.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                items(transactions.take(10), key = { it.id }) { transaction ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(1000, delayMillis = 500))
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

@Composable
private fun PrimaryCard(
    user: User?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SurfaceDark,
                        BinanceGreen.copy(alpha = 0.25f),
                        SurfaceDark
                    )
                )
            )
            .testTag("primary_card")
    ) {
        // Decorative circles for card texture
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 220.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(BinanceGreen.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 260.dp, y = 80.dp)
                .clip(CircleShape)
                .background(BinanceGreen.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: chip + contactless icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SIM chip
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BinanceGreen.copy(alpha = 0.4f))
                )
                Icon(
                    imageVector = Icons.Filled.Contactless,
                    contentDescription = "Contactless",
                    tint = BinanceGreen.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Card number
            Text(
                text = "**** **** **** 4242",
                color = TextPrimary,
                fontSize = 18.sp,
                fontFamily = RobotoMono,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )

            // Bottom row: cardholder + expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "CARDHOLDER", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text(
                        text = user?.name?.uppercase() ?: "— —",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = RobotoMono
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "EXPIRES", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text(
                        text = "12/28",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = RobotoMono
                    )
                }
            }
        }
    }
}

@Composable
private fun CardDetailChip(
    label: String,
    value: String,
    icon: ImageVector,
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
                        .background(BinanceGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BinanceGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoMono
            )
        }
    }
}

@Composable
private fun CardAction(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .glassmorphism(cornerRadius = 18.dp, alpha = 0.4f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BinanceGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}
