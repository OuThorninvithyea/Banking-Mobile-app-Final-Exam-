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
import androidx.compose.foundation.BorderStroke
import com.hustle.bankapp.data.BankRepository
import com.hustle.bankapp.theme.*
import com.hustle.bankapp.ui.components.glassmorphism
import com.hustle.bankapp.ui.dashboard.formatAsCurrency
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

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

private enum class ReceivePhase { QR_DISPLAY, SET_AMOUNT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    repository: BankRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(ReceivePhase.QR_DISPLAY) }
    var amountString by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var userId by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var fixedAmount by remember { mutableDoubleStateOf(0.0) }

    val amountAsDouble = amountString.toDoubleOrNull() ?: 0.0

    LaunchedEffect(Unit) {
        try {
            val profile = repository.getUserProfile().catch { emit(null) }.first()
            userId = profile?.id ?: ""
            accountNumber = profile?.accountNumber ?: ""
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
        isLoading = false
    }

    fun updateAmount(input: String) {
        if (input == "DELETE") {
            if (amountString.isNotEmpty()) {
                amountString = amountString.dropLast(1)
            }
            return
        }
        if (input == "." && amountString.contains(".")) return
        val newStr = if (amountString == "0" && input != ".") input else amountString + input
        if (newStr.substringAfter(".", "").length > 2) return
        amountString = newStr
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (phase == ReceivePhase.QR_DISPLAY) "Receive Money" else "Set Amount",
                        color = BinanceGreen,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (phase == ReceivePhase.SET_AMOUNT) phase = ReceivePhase.QR_DISPLAY
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
            ReceivePhase.QR_DISPLAY -> {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BinanceGreen)
                    }
                } else {
                    val amountParam = if (fixedAmount > 0) "&amount=$fixedAmount" else ""
                    val qrContent = "hustlebank://pay?id=$userId&account=$accountNumber$amountParam"
                    val qrBitmap = remember(qrContent) { generateQrBitmap(qrContent) }

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (fixedAmount > 0) {
                                    Text("Requesting", color = TextSecondary, fontSize = 14.sp)
                                    Text(
                                        fixedAmount.formatAsCurrency(),
                                        color = BinanceGreen,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = RobotoMono
                                    )
                                } else {
                                    Text("Scan to pay me", color = TextSecondary, fontSize = 14.sp)
                                    Text(
                                        "Any amount",
                                        color = Color(0xFFF5A623),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphism(cornerRadius = 24.dp, alpha = 0.3f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                    Text("Account", color = TextSecondary, fontSize = 12.sp)
                                    Text(
                                        accountNumber,
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = RobotoMono
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        OutlinedButton(
                            onClick = { phase = ReceivePhase.SET_AMOUNT },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceGreen),
                            border = BorderStroke(1.dp, BinanceGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                if (fixedAmount > 0) "Change Amount" else "Set Amount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinanceGreen,
                                contentColor = BackgroundBlack
                            ),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            ReceivePhase.SET_AMOUNT -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            fixedAmount = if (amountAsDouble <= 0) 0.0 else amountAsDouble
                            phase = ReceivePhase.QR_DISPLAY
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceGreen,
                            contentColor = BackgroundBlack
                        ),
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Update QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp)
                    }

                    if (fixedAmount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = {
                            fixedAmount = 0.0
                            amountString = ""
                            phase = ReceivePhase.QR_DISPLAY
                        }) {
                            Text("Remove amount (accept any)", color = ErrorRed, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
