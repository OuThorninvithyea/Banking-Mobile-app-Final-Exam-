# Task: UI Redesign - Layouts & Screens

## Context
You must previously complete `1_theme_foundation.md` before executing these instructions. 
We are combining our existing 2-color brand identity (`BinanceGreen` `#0ECB81` & `BackgroundBlack` `#0B0E11`) with the modern structural layout of `ChanthaSinat/Banking_App_UI_HTML` (Glass cards, elevated nav bars, semantic icons).

## Action Required

Please rewrite the main application screens using the newly created `GlassCard` and `glassmorphism()` modifiers to match the spatial layout of the HTML template.

### Specific Instructions for Cursor

1.  **Rebuild Navigation Base (`ui/navigation/BottomNavBar.kt`)**
    *   Create a floating Bottom Navigation Bar. Give it a transparent background, but use a `.glassmorphism()` container matching the HTML `<nav>`.
    *   It should anchor to the bottom with spacing on the sides, using `BackgroundBlack` with 90% opacity as the glass container color.
    *   It should contain 5 items: Home, Account, a **prominent center Scan/QR button**, Cards, Settings.
    *   The prominent center button should break out of the top bounds of the nav bar (just like the HTML `.nav-btn-scan`). Color this giant center FAB solid `BinanceGreen`.

2.  **Rewrite Authentication Screens (`ui/auth/LoginScreen.kt` & `RegisterScreen.kt`)**
    *   Remove any massive solid-color headers.
    *   Structure: Left-aligned bold title "Welcome back" (or "Create account") followed by muted subtitle.
    *   Form: Use exactly the `OutlinedInputField` we built in the previous step. Wrap the entire form in a `GlassCard` (with `SurfaceDark` alpha).
    *   Action: Use the solid `BinanceGreen` `BrandButton` for "Log In" / "Register".
    *   Add a flat textual icon below the button for Biometric prompt (using the `BiometricPromptManager` already initialized).

3.  **Rewrite Home Dashboard (`ui/dashboard/DashboardScreen.kt` or `HomeScreen.kt`)**
    *   **Background:** Must be pure `BackgroundBlack`. No white padding anywhere.
    *   **Header Row (Sticky/Glass):** Avatar icon on the left ("Welcome back, Alex"). Notification Bell on the right inside a tiny `.glassmorphism()` circle button.
    *   **Balance Section:** Large text (e.g., "$42,850.45") with an "eye-off" toggle next to it to blur the text.
    *   **Quick Actions (The 4 Icon Grid):** 
        *   Row of 4 evenly spaced items: Transfer, Deposit, Withdraw, Summary/Bills.
        *   Each item is a square `GlassCard` container.
        *   Inside the GlassCard is a Lucide-style Icon tinted `BinanceGreen` (since we are not using the multi-color HTML purple/rose/amber semantic colors, all actions use our primary brand green).
    *   **Overview Cards (Spent vs Income):** 
        *   A 2-column grid. Left card = "Spent this month", Right card = "Income this month". 
        *   Both are `GlassCard` components.
    *   **Smart Insights / Recent Transactions:**
        *   A single wide `GlassCard`. Inside, a vertical list of recent transactions or "insights", each with a tinted green leading icon.

*Tip for Cursor:* Try executing these instructions to completely rewrite `LoginScreen.kt` and `HomeScreen.kt`. Build the project after to ensure the new glass structures compile against the existing Compose state flows!
