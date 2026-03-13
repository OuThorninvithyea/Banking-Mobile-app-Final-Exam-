package com.hustle.bankapp.ui.transaction

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.glassmorphism
import com.hustle.bankapp.ui.dashboard.formatAsCurrency
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val white = Color.White.toArgb()
    val dark = Color(0xFF0B0E11).toArgb()
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) dark else white)
        }
    }
    return bitmap
}

private enum class ReceivePhase { AMOUNT_ENTRY, QR_DISPLAY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    repository: BankRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(ReceivePhase.AMOUNT_ENTRY) }
    var amountString by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var userId by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var depositedAmount by remember { mutableDoubleStateOf(0.0) }

    val scope = rememberCoroutineScope()
    val amountAsDouble = amountString.toDoubleOrNull() ?: 0.0

    fun updateAmount(input: String) {
        if (input == "DELETE") {
            if (amountString.isNotEmpty()) {
                amountString = amountString.dropLast(1)
                error = null
            }
            return
        }
        if (input == "." && amountString.contains(".")) return
        val newStr = if (amountString == "0" && input != ".") input else amountString + input
        if (newStr.substringAfter(".", "").length > 2) return
        amountString = newStr
        error = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (phase == ReceivePhase.AMOUNT_ENTRY) "Receive Money" else "Your QR Code",
                        color = BinanceGreen,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (phase == ReceivePhase.QR_DISPLAY) phase = ReceivePhase.AMOUNT_ENTRY
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlack)
            )
        }
    ) { innerPadding ->
        when (phase) {
            ReceivePhase.AMOUNT_ENTRY -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (error != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphism(alpha = 0.2f, borderColor = ErrorRed.copy(alpha = 0.5f))
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = error!!,
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.5f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphism(cornerRadius = 32.dp, alpha = 0.2f)
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$${amountString.ifEmpty { "0.00" }}",
                            color = if (amountString.isEmpty()) TextSecondary else BinanceGreen,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RobotoMono
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TransactionNumberPad(
                        onNumberClick = { updateAmount(it.toString()) },
                        onDecimalClick = { updateAmount(".") },
                        onDeleteClick = { updateAmount("DELETE") }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (amountAsDouble <= 0) {
                                error = "Amount must be greater than \$0.00."
                                return@Button
                            }
                            isLoading = true
                            error = null

                            scope.launch {
                                try {
                                    val profile = repository.getUserProfile()
                                        .catch { emit(null) }
                                        .first()

                                    userId = profile?.id ?: ""
                                    accountNumber = profile?.accountNumber ?: ""
                                    depositedAmount = amountAsDouble
                                    isLoading = false
                                    phase = ReceivePhase.QR_DISPLAY
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                    isLoading = false
                                    error = e.message ?: "Failed to load profile"
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceGreen,
                            contentColor = BackgroundBlack,
                            disabledContainerColor = SurfaceDark,
                            disabledContentColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BackgroundBlack, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Generate QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            ReceivePhase.QR_DISPLAY -> {
                val qrContent = "hustlebank://pay?id=$userId&account=$accountNumber&amount=$depositedAmount"
                val qrBitmap = remember(qrContent) { generateQrBitmap(qrContent) }

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Requesting Payment",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                depositedAmount.formatAsCurrency(),
                                color = BinanceGreen,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = RobotoMono
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Waiting for sender to scan & transfer",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphism(cornerRadius = 24.dp, alpha = 0.3f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Scan to send me money",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (accountNumber.isNotEmpty()) {
                                Text(
                                    "Account",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    accountNumber,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = RobotoMono
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                "Amount",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                depositedAmount.formatAsCurrency(),
                                color = BinanceGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RobotoMono
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceGreen,
                            contentColor = BackgroundBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
