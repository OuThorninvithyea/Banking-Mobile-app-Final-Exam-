package com.hustle.bankapp.ui.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.Card
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.GlassCard
import com.hustle.bankapp.ui.components.glassmorphism
import com.hustle.bankapp.ui.dashboard.formatAsCurrency

private val cardTypes = listOf("Virtual", "Physical", "Business", "Prepaid")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardsScreen(
    viewModel: CardsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cards = uiState.cards

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Pager state — page count driven by card list
    val pagerState = rememberPagerState(pageCount = { maxOf(1, cards.size) })

    // Keep ViewModel selectedCardIndex in sync with pager
    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectCard(pagerState.currentPage)
    }

    val activeCard = cards.getOrNull(pagerState.currentPage)

    // Dialog visibility
    var showFreezeDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showReplaceDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }

    var limitInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Virtual") }

    // Auto-clear error
    uiState.error?.let {
        LaunchedEffect(it) { viewModel.clearError() }
    }

    // ── Dialogs ─────────────────────────────────────────────────────────

    if (showFreezeDialog && activeCard != null) {
        val action = if (activeCard.isFrozen) "Unfreeze" else "Freeze"
        AlertDialog(
            onDismissRequest = { showFreezeDialog = false },
            containerColor = SurfaceDark,
            title = { Text("$action Card", color = TextPrimary) },
            text = { Text("Are you sure you want to $action this card?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleFreeze(activeCard.id); showFreezeDialog = false }) {
                    Text(action, color = BinanceGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFreezeDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (showLimitDialog && activeCard != null) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Update Spending Limit", color = TextPrimary) },
            text = {
                Column {
                    Text("Current: ${activeCard.limit.formatAsCurrency()}", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        label = { Text("New Limit ($)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinanceGreen,
                            unfocusedBorderColor = SurfaceDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    limitInput.toDoubleOrNull()?.let { viewModel.updateLimit(activeCard.id, it) }
                    showLimitDialog = false; limitInput = ""
                }) { Text("Update", color = BinanceGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false; limitInput = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Block Card", color = ErrorRed) },
            text = { Text("Blocking is permanent and cannot be undone. Contact support to issue a replacement.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Block", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (showReplaceDialog) {
        AlertDialog(
            onDismissRequest = { showReplaceDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Replace Card", color = TextPrimary) },
            text = { Text("A new virtual card will be issued. Your current card will be deactivated.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.createCard(); showReplaceDialog = false }) {
                    Text("Replace", color = BinanceGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (showEditDialog && activeCard != null) {
        LaunchedEffect(activeCard.id) { selectedType = activeCard.type }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Edit Card", color = TextPrimary) },
            text = {
                Column {
                    Text("Card Type", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    cardTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = BinanceGreen)
                            )
                            Text(type, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateCardInfo(activeCard.id, selectedType)
                    showEditDialog = false
                }) { Text("Save", color = BinanceGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (showLinkDialog && activeCard != null) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Link to Account", color = TextPrimary) },
            text = {
                Column {
                    if (activeCard.linkedAccountName != null) {
                        Text(
                            "Currently linked to: ${activeCard.linkedAccountName}",
                            color = BinanceGreen,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    if (uiState.availableAccounts.isEmpty()) {
                        Text("No accounts available", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        Text("Select an account:", color = TextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        uiState.availableAccounts.forEach { account ->
                            val isLinked = activeCard.linkedAccountId == account.id
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLinked) BinanceGreen.copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = isLinked,
                                    onClick = {
                                        viewModel.linkCardToAccount(activeCard.id, account.id)
                                        showLinkDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = BinanceGreen)
                                )
                                Column {
                                    Text(account.accountName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(account.accountNumber, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    // ── Screen ───────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
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

            // ── HEADER ───────────────────────────────────────────────────
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
                            fontFamily = com.hustle.bankapp.theme.Inter
                        )
                        IconButton(
                            onClick = { viewModel.createCard() },
                            modifier = Modifier
                                .size(40.dp)
                                .glassmorphism(cornerRadius = 20.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = BinanceGreen,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
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
            }

            // ── HORIZONTAL PAGER ─────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { 40 }
                ) {
                    Column {
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            pageSpacing = 12.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val card = cards.getOrNull(page)
                            if (card == null) {
                                EmptyCardPlaceholder()
                            } else {
                                PrimaryCard(card = card)
                            }
                        }

                        // Pager dots
                        if (cards.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(cards.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(if (pagerState.currentPage == index) 8.dp else 5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (pagerState.currentPage == index) BinanceGreen
                                                else TextSecondary.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── CARD DETAILS ROW ─────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 40 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CardDetailChip(
                                label = "Card Limit",
                                value = activeCard?.limit?.formatAsCurrency() ?: "—",
                                icon = Icons.Filled.CreditScore,
                                modifier = Modifier.weight(1f)
                            )
                            CardDetailChip(
                                label = "Status",
                                value = if (activeCard?.isFrozen == true) "Frozen" else if (activeCard != null) "Active" else "—",
                                icon = if (activeCard?.isFrozen == true) Icons.Filled.AcUnit else Icons.Filled.CheckCircle,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        CardDetailChip(
                            label = "Linked Account",
                            value = activeCard?.linkedAccountName ?: "Not linked",
                            icon = Icons.Filled.AccountBalance,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── CARD CONTROLS ────────────────────────────────────────────
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
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CardAction(
                                icon = Icons.Filled.AcUnit,
                                label = if (activeCard?.isFrozen == true) "Unfreeze" else "Freeze",
                                enabled = activeCard != null,
                                onClick = { showFreezeDialog = true }
                            )
                            CardAction(
                                icon = Icons.Filled.Block,
                                label = "Block",
                                enabled = activeCard != null,
                                tint = ErrorRed,
                                onClick = { showBlockDialog = true }
                            )
                            CardAction(
                                icon = Icons.Filled.Tune,
                                label = "Limits",
                                enabled = activeCard != null,
                                onClick = { showLimitDialog = true }
                            )
                            CardAction(
                                icon = Icons.Filled.Edit,
                                label = "Edit",
                                enabled = activeCard != null,
                                onClick = { showEditDialog = true }
                            )
                            CardAction(
                                icon = Icons.Filled.Link,
                                label = "Link",
                                enabled = activeCard != null,
                                onClick = { showLinkDialog = true }
                            )
                            CardAction(
                                icon = Icons.Filled.Autorenew,
                                label = "Replace",
                                enabled = activeCard != null,
                                onClick = { showReplaceDialog = true }
                            )
                        }
                    }
                }
            }

            // ── ERROR ─────────────────────────────────────────────────────
            uiState.error?.let {
                item {
                    Text(
                        text = it,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCardPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.CreditCard, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text("No cards yet. Tap + to add one.", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PrimaryCard(card: Card, modifier: Modifier = Modifier) {
    val maskedNumber = "**** **** **** ${card.number.takeLast(4)}"
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SurfaceDark, BinanceGreen.copy(alpha = 0.25f), SurfaceDark)
                )
            )
            .testTag("primary_card")
    ) {
        Box(modifier = Modifier.size(180.dp).offset(x = 220.dp, y = (-50).dp).clip(CircleShape).background(BinanceGreen.copy(alpha = 0.07f)))
        Box(modifier = Modifier.size(120.dp).offset(x = 260.dp, y = 80.dp).clip(CircleShape).background(BinanceGreen.copy(alpha = 0.05f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(width = 40.dp, height = 30.dp).clip(RoundedCornerShape(4.dp)).background(BinanceGreen.copy(alpha = 0.4f))
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (card.isFrozen) {
                        Icon(Icons.Filled.AcUnit, contentDescription = "Frozen", tint = Color(0xFF87CEEB), modifier = Modifier.size(16.dp))
                    }
                    Text(card.type.uppercase(), color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                    Icon(Icons.Filled.Contactless, contentDescription = null, tint = BinanceGreen.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                }
            }

            Text(maskedNumber, color = TextPrimary, fontSize = 18.sp, fontFamily = RobotoMono, fontWeight = FontWeight.Medium, letterSpacing = 3.sp)

            if (card.linkedAccountName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Link, contentDescription = null, tint = BinanceGreen.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(card.linkedAccountName, color = BinanceGreen.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("CVV", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text(card.cvv, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = RobotoMono)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EXPIRES", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text(card.expiry, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = RobotoMono)
                }
            }
        }
    }
}

@Composable
private fun CardDetailChip(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Box(modifier = modifier.glassmorphism(cornerRadius = 20.dp, alpha = 0.4f)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(BinanceGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = BinanceGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        }
    }
}

@Composable
private fun CardAction(icon: ImageVector, label: String, enabled: Boolean = true, tint: Color = BinanceGreen, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(52.dp)
                .glassmorphism(cornerRadius = 18.dp, alpha = if (enabled) 0.4f else 0.2f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) tint else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
    }
}
