# Task: Implement QR Scanner UI

## Context
We are implementing Appium end-to-end automation testing. We need a way for the Appium scripts to navigate to a QR Code Scanner screen and interact with a Camera layout. To test Camera Image Injection (the ability to mock what the camera sees in the test script), we need to create the UI for it first.

## Action Required

### 1. Add "Scan QR" Button to Dashboard
1. Open `app/src/main/java/com/hustle/bankapp/ui/dashboard/DashboardScreen.kt`.
2. In the `ActionButtonsRow`, we currently have Deposit, Withdraw, and Transfer. We need to add a way to scan a QR code to quickly pre-fill a transfer.
3. Replace the `onTransferClick` outline button with a row/grid that includes a new button for "Scan QR". Or simply add it to the top app bar as an action icon (e.g., `Icons.Filled.QrCodeScanner`).
4. **CRITICAL:** Add `Modifier.testTag("scan_qr_button")` to this new QR Scan button so the UI automation script can find it and click it.

### 2. Create a basic QRScannerScreen
1. Create a new file `app/src/main/java/com/hustle/bankapp/ui/transfer/QRScannerScreen.kt`.
2. Inside, create a `@Composable fun QRScannerScreen(...)`.
3. In a real application, we would use the AndroidX `CameraX` library to display a live camera feed. For the sake of UI development and UI automation testing preparation, you can just build a placeholder UI that looks like a Camera Viewfinder (e.g., a dark background with a transparent square cutout in the middle for the framing).
4. **CRITICAL:** Add `Modifier.testTag("scanner_view")` to the primary container or "camera preview" box in this screen. The Appium script will wait for this element to become visible before it attempts to inject the custom mock QR code via image injection.

### 3. Navigation Setup
1. Open `app/src/main/java/com/hustle/bankapp/MainActivity.kt`.
2. Add a new `composable("qr_scanner")` route in the `NavHost` that opens `QRScannerScreen`.
3. Ensure the newly created "Scan QR" button in the Dashboard triggers `navController.navigate("qr_scanner")`.
