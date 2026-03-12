# Execution Plan: HustleBank Polish & Features (Phase 1)

This plan focuses on high-impact UI/UX improvements and micro-interactions to make HustleBank feel premium and production-ready.

## Proposed Changes

### 1. Design System Enhancements
**Goal:** Add haptic feedback and shimmer loading effects.

#### [MODIFY] `CoreComponents.kt` (app/src/main/java/com/hustle/bankapp/ui/components/CoreComponents.kt)
- Add a custom `shimmerEffect()` modifier using `rememberInfiniteTransition`.
- Update `BrandButton` to include `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)` (or similar tap feedback).

### 2. Dashboard Micro-interactions
**Goal:** Smoother transitions and staggered animations.

#### [MODIFY] `DashboardScreen.kt` (app/src/main/java/com/hustle/bankapp/ui/dashboard/DashboardScreen.kt)
- Wrap balance text in `AnimatedContent` for the visibility toggle.
- Use `AnimatedVisibility` with `fadeIn + slideInVertically` for transaction list items with index-based delays.

### 3. Navigation Polish
**Goal:** Smoother screen transitions.

#### [MODIFY] `MainActivity.kt` (app/src/main/java/com/hustle/bankapp/MainActivity.kt)
- Define global `enterTransition` and `exitTransition` for the `NavHost` to use consistent slide/fade animations.

### 4. Spending Analytics (Quick Start)
**Goal:** Add category support to transactions.

#### [MODIFY] `transaction.go` (backend/internal/models/transaction.go)
- Add `Category string `json:"category"` `gorm:"type:varchar(50);default:'General'"` field.

#### [MODIFY] `transaction_repo.go` (backend/internal/repository/transaction_repo.go)
- Ensure the new `Category` field is handled in Create/Get operations.

## Verification Plan

### Automated Tests
- Run `./gradlew testDebugUnitTest` to ensure no regressions in ViewModels.
- Run `cd backend && go test ./...` to verify database migrations.

### Manual Verification
1. Open the app and toggle balance visibility; ensure it slides/fades smoothly.
2. Verify that transaction items "pop in" with a staggered delay.
3. Check that buttons provide a subtle haptic vibration on tap.
4. Verify that the new `category` field appears in the transaction JSON response.
