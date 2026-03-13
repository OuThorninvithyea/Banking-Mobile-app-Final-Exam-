package com.hustle.bankapp.ui.transfer

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.hustle.bankapp.theme.*
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onNavigateBack: () -> Unit = {},
    onQrCodeScanned: (String) -> Unit = {}
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text("Scan QR Code", color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = com.hustle.bankapp.theme.Inter)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
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
            when {
                cameraPermission.status.isGranted -> {
                    CameraPreviewWithScanner(
                        onQrCodeScanned = onQrCodeScanned
                    )
                }
                cameraPermission.status.shouldShowRationale -> {
                    PermissionRationale(onRequest = { cameraPermission.launchPermissionRequest() })
                }
                else -> {
                    PermissionDenied(onRequest = { cameraPermission.launchPermissionRequest() })
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewWithScanner(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasScanned = remember { AtomicBoolean(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Live camera feed
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val barcodeScanner = BarcodeScanning.getClient()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                processImageProxy(barcodeScanner, imageProxy, hasScanned, onQrCodeScanned)
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI on top of camera
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Point your camera at a QR code",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Viewfinder frame
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
                    .border(2.dp, BinanceGreen, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                CornerBracket(Alignment.TopStart)
                CornerBracket(Alignment.TopEnd)
                CornerBracket(Alignment.BottomStart)
                CornerBracket(Alignment.BottomEnd)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .padding(horizontal = 12.dp)
                        .offset(y = (scanLineY * 236).dp)
                        .background(BinanceGreen.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                )
            }

            Spacer(Modifier.height(32.dp))
            Text(
                text = "QR code will be detected automatically",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontFamily = com.hustle.bankapp.theme.Inter,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    hasScanned: AtomicBoolean,
    onQrCodeScanned: (String) -> Unit
) {
    if (hasScanned.get()) {
        imageProxy.close()
        return
    }

    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                ?.rawValue
                ?.let { value ->
                    if (hasScanned.compareAndSet(false, true)) {
                        onQrCodeScanned(value)
                    }
                }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = BinanceGreen, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Camera Access Needed", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Camera permission is required to scan QR codes for transfers.", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = BinanceGreen, contentColor = BackgroundBlack)
        ) { Text("Grant Permission", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun PermissionDenied(onRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Camera Permission Denied", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Please enable camera access in your device Settings to use QR scanning.", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRequest,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceGreen)
        ) { Text("Try Again") }
    }
}

@Composable
private fun BoxScope.CornerBracket(alignment: Alignment) {
    val isTop = alignment == Alignment.TopStart || alignment == Alignment.TopEnd
    val isStart = alignment == Alignment.TopStart || alignment == Alignment.BottomStart
    val size = 24.dp
    val thickness = 3.dp

    Box(modifier = Modifier.align(alignment).padding(8.dp).size(size)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .align(if (isTop) Alignment.TopCenter else Alignment.BottomCenter)
                .background(BinanceGreen, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(thickness)
                .fillMaxHeight()
                .align(if (isStart) Alignment.CenterStart else Alignment.CenterEnd)
                .background(BinanceGreen, RoundedCornerShape(1.dp))
        )
    }
}
