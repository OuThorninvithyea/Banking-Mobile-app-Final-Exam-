# REFINED INSTRUCTIONS FOR HOME PAGE REDESIGN

Redesign the `DashboardScreen.kt` layout to match the reference layout precisely, fixing alignment, button sizes, and naming.

## Design Requirements
- **Grid Layout**: Use a **3-column grid** (2 rows) for the 6 services ("Cards", "Accounts", etc.). This is critical for matching the reference image.
- **Service Card (Vertical)**: The `ServiceGridCard` MUST use a vertical `Column` layout (Icon above, Label below) instead of a horizontal `Row`.
- **Button Sizes**: Make all buttons (jars/grid items) smaller and more square-ish to avoid them looking "too big".
- **Color Palette**: Use existing `BackgroundBlack`, `BinanceGreen`, `SurfaceDark`, `TextPrimary`, `TextSecondary`.

## Detailed Layout Structure

### 1. Header (Top Bar)
- **Profile**: Circular image placeholder on the far left.
- **Greeting**: "Good afternoon!" (secondary text) and "Thorninvithyea" (primary bold text) in a column to the right of the profile picture.
- **Utilities**: Three icons on the right (Message, Notification with badge, QR Code). Space them reasonably.

### 2. Primary Balance Card (Hero)
- **Background**: High-opacity `GlassCard` or horizontal green-to-black gradient.
- **Top Row**: "USD" or balance label and an eye icon button to toggle visibility.
- **Balance**: Large bold balance text (use `isBalanceVisible` logic).
- **Badge**: A "Default" badge/chip next to the balance label.
- **Mascot**: A generic bank or lion icon (e.g., `Icons.Filled.AccountBalance`) on the right side.
- **Action Buttons**: A horizontal row at the bottom with: `Receive`, `Send`, `Analytics`. Use smaller icons (maybe 20.dp) and compact labels (11.sp or 12.sp).

### 3. Services Grid (3-Column Fix)
- Create a 2x3 grid (3 items per row).
- **Layout per Item**: `GlassCard` -> `Column` -> `Box(Icon)` -> `Text(Label)`.
- **Renaming**: The 4th item (formerly "ABA Scan" or "ABA Pay") should be renamed to **"Pay"**.
- **Items**:
    1. **Cards** (Red icon)
    2. **Accounts** (Gold/Yellow icon)
    3. **Payments** (Orange/Gold icon)
    4. **Pay** (Red icon) - *Formerly ABA Scan*
    5. **Favorites** (Gold/Yellow icon)
    6. **Transfers** (Red/Orange icon)

### 4. Navigation Chips
- A horizontal scroll (`LazyRow`) of small pills: "Mini Apps", "ABA Merchant", "Rewards", etc.

### 5. News & Promotions
- A banner section showing a promo card (e.g., "Home Loan") with a CTA.

## Implementation Guide
- Modify `DashboardScreen.kt`.
- Update `ServiceGridCard` to be vertical and more compact.
- Ensure the services list has 3 columns (use nested Rows or a grid-like flow).
- Rename "ABA Scan" to "Pay".
