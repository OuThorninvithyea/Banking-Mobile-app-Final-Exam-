package com.hustle.bankapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hustle.bankapp.theme.BackgroundBlack
import com.hustle.bankapp.theme.BinanceGreen
import com.hustle.bankapp.theme.TextPrimary
import com.hustle.bankapp.theme.TextSecondary
import com.hustle.bankapp.ui.components.glassmorphism

@Composable
fun MainBottomNavBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Only show the floating nav bar on these three main screens
    val showBottomBar = currentRoute in listOf("dashboard", "history", "profile", "cards")
    if (!showBottomBar) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Floating spacing from the bottom edge
            .background(Color.Transparent)
    ) {
        // The Glass Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .glassmorphism(cornerRadius = 36.dp, alpha = 0.85f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Filled.Home,
                label = "Home",
                isSelected = currentRoute == "dashboard",
                onClick = { navController.navigate("dashboard") { launchSingleTop = true; restoreState = true } },
                modifier = Modifier.weight(1f)
            )
            
            NavBarItem(
                icon = Icons.Filled.CreditCard,
                label = "Cards",
                isSelected = currentRoute == "cards",
                onClick = { navController.navigate("cards") { launchSingleTop = true; restoreState = true } },
                modifier = Modifier.weight(1f)
            )

            // Center Spacer for the giant floating button
            Spacer(modifier = Modifier.weight(1f))

            NavBarItem(
                icon = Icons.Filled.History,
                label = "History",
                isSelected = currentRoute == "history",
                onClick = { navController.navigate("history") { launchSingleTop = true; restoreState = true } },
                modifier = Modifier.weight(1f)
            )

            NavBarItem(
                icon = Icons.Filled.Person,
                label = "Profile",
                isSelected = currentRoute == "profile",
                onClick = { navController.navigate("profile") { launchSingleTop = true; restoreState = true } },
                modifier = Modifier.weight(1f)
            )
        }

        // The Floating Center Action Button (QR Scan / Primary Action) breaking bounds
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(BinanceGreen)
                .clickable { navController.navigate("qr_scanner") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = "Scan QR",
                tint = BackgroundBlack,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) BinanceGreen else TextSecondary
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp
        )
    }
}
