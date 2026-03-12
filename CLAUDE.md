# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HustleBank - a full-stack Android banking app with a Kotlin/Jetpack Compose frontend and a Go backend.

## Build & Run Commands

### Android App (root directory)
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device/emulator
./gradlew testDebugUnitTest      # Run unit tests
./gradlew connectedDebugAndroidTest  # Run instrumented tests on device
./gradlew clean                  # Clean build artifacts
```

### Backend (backend/)
```bash
cd backend && go run cmd/server/main.go   # Run backend locally
cd backend && go build ./...               # Build backend
cd backend && go test ./...                # Run backend tests
```

### E2E Tests (e2e-tests/)
Uses WebdriverIO + Appium with UiAutomator2 driver. Run `npx wdio run wdio.conf.ts` from the `e2e-tests/` directory.

## Architecture

### Android App (`app/src/main/java/com/hustle/bankapp/`)

**MVVM + Repository pattern with Jetpack Compose (no XML layouts).**

- **`MainActivity.kt`** - Single activity, hosts the NavHost with all routes. All ViewModels are instantiated here and passed to composable screens.
- **`data/BankRepository.kt`** - Repository interface defining all banking operations (balance, transfers, cards, auth). Two implementations: `RemoteBankRepositoryImpl` (Retrofit) and `MockBankRepositoryImpl`.
- **`data/api/BankApiService.kt`** - Retrofit interface. Base URL comes from `BuildConfig.BASE_URL` (debug: `http://10.0.2.2:8080/`, release: `https://hustlebank-api.onrender.com/`).
- **`data/TokenManager.kt`** - JWT token persistence via SharedPreferences. `AuthInterceptor` injects the Bearer token into all requests.
- **`ui/`** - Feature screens organized by domain: `auth/`, `dashboard/`, `cards/`, `transfer/`, `history/`, `profile/`, `transaction/`. Each has its own ViewModel and UiState.
- **`ui/components/`** - Shared composables: `GlassCard`, `BrandButton`, `OutlinedInputField`, glassmorphism modifier.
- **`theme/`** - Dark theme with Binance-inspired green (`#0ECB81`) color scheme.

### State Management
- ViewModels expose `StateFlow<UiState>` to composables
- Repository methods return `Flow` or `Result<T>` for error handling
- `WhileSubscribed(5000)` sharing strategy on derived flows

### Backend (`backend/`)
Go backend using Gin framework, GORM with PostgreSQL, JWT auth, deployed on Render. API routes under `/api/auth/`, `/api/user/`, `/api/transactions/`, `/api/cards/`.

## Key Configuration

- **compileSdk/targetSdk:** 34, **minSdk:** 24, **JVM target:** 17
- **Compose BOM:** 2024.02.01, **Compose Compiler:** 1.5.14
- **Kotlin:** 1.9.24, **AGP:** 8.13.2
- **OkHttp timeouts:** 15s connect/read/write
- Cleartext traffic enabled only in debug (for emulator → localhost)

## Navigation Routes

`login` | `register` | `dashboard` | `cards` | `transfer?recipient={}` | `qr_scanner` | `deposit` | `withdraw` | `history` | `profile`

Bottom nav bar appears on: dashboard, cards, history, profile.