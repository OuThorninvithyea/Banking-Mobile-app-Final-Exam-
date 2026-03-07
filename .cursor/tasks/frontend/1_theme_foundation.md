# Task: UI Redesign - Theme & Foundation

## Context
We are completely overhauling the Android app's UI to adopt a high-end "Glassmorphism" layout structure based on a modern HTML template, but we are **strictly preserving** our existing 2-color brand identity: `BinanceGreen` (`#0ECB81`) and `BackgroundBlack` (`#0B0E11`) / `SurfaceDark` (`#1E2329`).

We need to set up the foundation for this UI in Jetpack Compose: The Typography, the Base Theme, and the core structural UI components.

## Action Required

Please generate the foundational Compose UI code in `app/src/main/java/com/hustle/bankapp/ui/`.

### Specific Instructions for Cursor

1.  **Update Typography (`ui/theme/Type.kt`)**
    *   Change the default font family for all TextStyles to `PlusJakartaSans`.
    *   *Note: If you need to add Google Fonts, you can use the standard Compose Material 3 `Typography` definitions, or manually define the `FontFamily` if you download the .ttf files to `res/font/`. For now, just define a `FontFamily` in `Type.kt` pointing to `R.font.plus_jakarta_sans` (assume the fonts are downloaded, or use standard `FontFamily.SansSerif` as a fallback if they are missing).*

2.  **Create the Glassmorphism Modifier (`ui/components/Glassmorphism.kt`)**
    *   Create a new file `ui/components/Glassmorphism.kt`.
    *   Write a custom modifier extension function: `fun Modifier.glassmorphism(cornerRadius: Dp = 24.dp, alpha: Float = 0.7f): Modifier`.
    *   This modifier should apply a background color of `SurfaceDark.copy(alpha = alpha)` and a subtle border stroke of `1.dp` with `Color.White.copy(alpha = 0.08f)`.
    *   It should clip to a `RoundedCornerShape(cornerRadius)`.
    *   *(Note: True Compose blur (`blur()` modifier) drops performance drastically on old devices, so creating this alpha-layered look over `BackgroundBlack` perfectly simulates the `.glass` CSS from the HTML).*

3.  **Build the Core Reusable Components (`ui/components/CoreComponents.kt`)**
    *   Create a new file `ui/components/CoreComponents.kt` and build these composables:
    
    *   **`GlassCard(modifier: Modifier, content: @Composable ColumnScope.() -> Unit)`**
        *   A standard `Column` wrapped in the new `.glassmorphism()` modifier. It should have default padding (e.g., `16.dp`).
        
    *   **`BrandButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier)`**
        *   A `Button` matching the `.card-gradient` HTML structure but colored in solid `BinanceGreen`. The text inside must be `BackgroundBlack` for high contrast (or whatever color ensures WCAG compliance on `#0ECB81`).
        *   It should have a `RoundedCornerShape(24.dp)` and minimum height of `56.dp`.

    *   **`OutlinedInputField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier)`**
        *   A custom `OutlinedTextField` mimicking the HTML `.bg-slate-800`.
        *   **Background:** `SurfaceDark.copy(alpha = 0.5f)`.
        *   **Colors:** `TextFieldDefaults.outlinedTextFieldColors` configuring the unfocused border to transparent, and the focused border to `BinanceGreen`.
        *   Shape should be `RoundedCornerShape(16.dp)`.

4.  **Verify the Theme (`ui/theme/Theme.kt` and `ui/theme/Color.kt`)**
    *   Ensure `Color.kt` retains:
        ```kotlin
        val BinanceGreen = Color(0xFF0ECB81)
        val BackgroundBlack = Color(0xFF0B0E11)
        val SurfaceDark = Color(0xFF1E2329)
        val ErrorRed = Color(0xFFF6465D)
        val TextPrimary = Color(0xFFEAECEF)
        val TextSecondary = Color(0xFF848E9C)
        ```
    *   Ensure `Theme.kt` forces Dark colors for everything (e.g., `background = BackgroundBlack`, `surface = SurfaceDark`, `primary = BinanceGreen`).

*Tip for Cursor:* Execute these instructions to build the structural foundation. We will rebuild the actual Dashboard/Home screens using these components in the next step.
