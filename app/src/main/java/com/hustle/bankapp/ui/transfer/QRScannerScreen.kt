package com.hustle.bankapp.ui.transfer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustle.bankapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Scanning line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan QR Code",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RobotoMono
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black)
                .testTag("scanner_view"),
            contentAlignment = Alignment.Center
        ) {
            // Dark overlay with a transparent cutout effect via layered boxes
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Point your camera at a QR code",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Viewfinder frame
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(2.dp, BinanceGreen, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Corner brackets — top-left
                    CornerBracket(Alignment.TopStart)
                    CornerBracket(Alignment.TopEnd)
                    CornerBracket(Alignment.BottomStart)
                    CornerBracket(Alignment.BottomEnd)

                    // Animated scan line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(horizontal = 12.dp)
                            .offset(y = (scanLineY * 236).dp)
                            .background(
                                BinanceGreen.copy(alpha = 0.8f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "QR code will be detected automatically",
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = RobotoMono,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoxScope.CornerBracket(alignment: Alignment) {
    val isTop = alignment == Alignment.TopStart || alignment == Alignment.TopEnd
    val isStart = alignment == Alignment.TopStart || alignment == Alignment.BottomStart
    val size = 24.dp
    val thickness = 3.dp

    Box(
        modifier = Modifier
            .align(alignment)
            .padding(8.dp)
            .size(size)
    ) {
        // Horizontal arm
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .align(if (isTop) Alignment.TopCenter else Alignment.BottomCenter)
                .background(BinanceGreen, RoundedCornerShape(1.dp))
        )
        // Vertical arm
        Box(
            modifier = Modifier
                .width(thickness)
                .fillMaxHeight()
                .align(if (isStart) Alignment.CenterStart else Alignment.CenterEnd)
                .background(BinanceGreen, RoundedCornerShape(1.dp))
        )
    }
}
