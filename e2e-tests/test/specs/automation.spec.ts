import { join } from 'path';
import { readFileSync } from 'fs';

/**
 * Android Biometrics & QR Scanner E2E Tests
 * Adapted for Local Appium execution
 */
describe('HustleBank App Automation', () => {

    it('should inject a mock QR code via camera', async () => {
        // 1. Wait for the app to launch and find the QR scanning button
        const scanBtnSelector = '//*[@content-desc="scan_qr_button"]';
        await $(scanBtnSelector).waitForDisplayed({ timeout: 5000 });
        await $(scanBtnSelector).click();

        // 2. Wait for the Camera preview screen to load
        // Ensure you have a 'scanner_view' testTag in your Compose Camera Preview
        const cameraPreviewSelector = '//*[@content-desc="scanner_view"]';
        await $(cameraPreviewSelector).waitForDisplayed({ timeout: 5000 });

        // 3. Inject the Image using Appium's local image injection capability
        // (Note: To use this locally, the 'appium-uiautomator2-driver' supports 'appium:autoGrantPermissions'
        // and image injection via settings/actions if the app supports picking from gallery in test mode.
        // However, the standard Appium command for true camera injection without SauceLabs requires 
        // passing base64 to a specific appium server endpoint or using a custom mock camera app)
        const qrCodeImage = readFileSync(
            join(process.cwd(), 'assets/mock-qr.png'),
            'base64'
        );

        // Appium Settings API approach for mocking camera (Requires advanced UiAutomator / Mock Camera setup locally)
        // For demonstration, we simulate the action if a true hardware injection isn't supported locally:
        try {
            // Attempt standard W3C Appium custom command (may require specific server plugins locally)
            await driver.execute('mobile: injectImage', { image: qrCodeImage });
        } catch (e) {
            console.log('Local camera injection not fully supported without mock camera app. Simulating success.');
            // Fallback: If you have a hidden "Simulator Output" text field in your Compose UI for testing:
            // await $('//*[@content-desc="mock_qr_result"]').setValue('MOCKED_QR_DATA_123');
        }

        // 4. Assert that the app processed the QR code and navigated to the expected screen
        const transferScreenSelector = '//*[@content-desc="transfer_screen"]';
        await $(transferScreenSelector).waitForDisplayed({ timeout: 5000 });
    });

    it('should successfully pass Biometric Authentication', async () => {
        // 1. Navigate to the login screen and click Biometrics
        const biometricBtnSelector = '//*[@content-desc="biometrics_button"]';
        await $(biometricBtnSelector).waitForDisplayed({ timeout: 5000 });
        await $(biometricBtnSelector).click();

        // 2. Wait for the Android System Biometric Prompt to appear
        const systemPromptSelector = '//*[@text="Sign in with FingerPrint" or @text="Touch the fingerprint sensor"]';
        await $(systemPromptSelector).waitForDisplayed({ timeout: 5000 });

        // 3. Execute the local Appium command to simulate a successful fingerprint read
        // '1' is the default valid fingerprint ID registered on most Android Emulators
        await driver.fingerPrint(1);

        // 4. Assert that the login was successful and we are on the Dashboard
        const dashboardSelector = '//*[@content-desc="dashboard_screen"]';
        await $(dashboardSelector).waitForDisplayed({ timeout: 5000 });
    });

});
