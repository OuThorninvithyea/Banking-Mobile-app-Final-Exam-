package com.hustle.bankapp.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.data.Contact
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSelectionScreen(
    viewModel: TransferSelectionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTransfer: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfers", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule", tint = TextPrimary)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = BackgroundBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Favorites Section
            item {
                SectionHeader("Favorites")
                FavoritesHorizontalList(uiState, onNavigateToTransfer)
            }

            // Local Transfers Section
            item {
                SectionHeader("Local Transfers")
                LocalTransfersGrid(onNavigateToTransfer)
            }

            // International Transfers Section
            item {
                SectionHeader("International Transfers")
                InternationalTransfersList()
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun FavoritesHorizontalList(
    state: TransferSelectionUiState,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (state) {
            is TransferSelectionUiState.Success -> {
                state.favorites.forEach { contact ->
                    FavoriteCircleItem(contact) { onSelect(contact.accountNumber) }
                }
            }
            is TransferSelectionUiState.Error -> {
                Text(
                    text = "Error loading favorites: ${state.message}",
                    color = ErrorRed,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCircleItem(contact: Contact, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(BinanceGreen.copy(alpha = 0.1f))
                .padding(2.dp)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase(),
                color = BinanceGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contact.name.uppercase(),
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocalTransfersGrid(onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TransferServiceCard(
                title = "Own Account",
                subtitle = "Make transfer to your own accounts",
                icon = Icons.Filled.AccountBalanceWallet,
                modifier = Modifier.weight(1f),
                onClick = { onSelect("") }
            )
            TransferServiceCard(
                title = "HustleBank Account",
                subtitle = "Transfer money to other HustleBank customers",
                icon = Icons.Filled.Person,
                modifier = Modifier.weight(1f),
                onClick = { onSelect("") }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TransferServiceCard(
                title = "Local Banks",
                subtitle = "Make transfer to banks or wallets in Cambodia",
                icon = Icons.Filled.AccountBalance,
                modifier = Modifier.weight(1f),
                onClick = { onSelect("") }
            )
            TransferServiceCard(
                title = "Cash-by-Code",
                subtitle = "Send cash with code to withdraw from any ATM",
                icon = Icons.Filled.QrCode,
                modifier = Modifier.weight(1f),
                onClick = { onSelect("") }
            )
        }
    }
}

@Composable
private fun InternationalTransfersList() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InternationalServiceItem("SWIFT - Wire Transfer", Icons.Filled.Public)
        InternationalServiceItem("Ria Money Send/Receive", Icons.AutoMirrored.Filled.CompareArrows)
        InternationalServiceItem("MoneyGram Send/Receive", Icons.AutoMirrored.Filled.CompareArrows)
    }
}

@Composable
private fun TransferServiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier
            .heightIn(min = 140.dp)
            .clickable(onClick = onClick),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BinanceGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun InternationalServiceItem(title: String, icon: ImageVector) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = BinanceGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
