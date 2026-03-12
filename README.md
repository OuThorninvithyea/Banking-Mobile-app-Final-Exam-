# HustleBank - Mobile Banking App

A full-stack Android banking application built with **Kotlin/Jetpack Compose** frontend and a **Go** backend.

## Features

- User registration & login with JWT authentication
- Dashboard with balance overview and transaction chart
- Deposit & withdraw funds
- Transfer money to other users
- QR code scanning for quick transfers
- Virtual card management (create, freeze, set limits)
- Transaction history
- Profile management
- Biometric authentication support
- Dark theme with glassmorphism UI

## Tech Stack

### Android App
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Repository pattern
- **Networking:** Retrofit + OkHttp
- **Navigation:** Jetpack Navigation Compose
- **Charts:** Vico
- **QR Scanning:** ML Kit + CameraX
- **Min SDK:** 24 | **Target SDK:** 34 | **JVM:** 17

### Backend
- **Language:** Go
- **Framework:** Gin
- **ORM:** GORM
- **Database:** PostgreSQL
- **Auth:** JWT
- **Deployment:** Render / Docker

## Prerequisites

- **Android Studio** (latest stable) with Android SDK 34
- **JDK 17**
- **Go 1.21+** (only if running the backend locally)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/Banking-Mobile-app-Final-Exam-.git
cd Banking-Mobile-app-Final-Exam-
```

### 2. Run the Android App

The app is pre-configured to connect to the hosted API at `api3.vvivid.dev`, so **no backend setup is needed** to run the app.

1. Open the project in **Android Studio**
2. Wait for Gradle sync to complete
3. Select an emulator or connected device
4. Click **Run** (or `Shift + F10`)

### 3. Run the Backend Locally (Optional)

Only needed if you want to run the backend on your own machine.

```bash
cd backend
```

Create a `.env` file in the `backend/` directory:

```env
DATABASE_URL=postgres://<user>:<password>@<host>:5432/<dbname>?sslmode=disable
JWT_SECRET=<your-jwt-secret>
PORT=8080
```

Run the server:

```bash
go run cmd/server/main.go
```

If running the backend locally with an Android emulator, update the API URL in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
```

### 4. Build Commands

#### Android App (from root directory)

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device/emulator
./gradlew testDebugUnitTest      # Run unit tests
./gradlew clean                  # Clean build artifacts
```

#### Backend (from backend/ directory)

```bash
go run cmd/server/main.go        # Run server
go build ./...                   # Build
go test ./...                    # Run tests
```

## Project Structure

```
Banking-Mobile-app-Final-Exam-/
├── app/src/main/java/com/hustle/bankapp/
│   ├── MainActivity.kt                 # Single activity, hosts NavHost
│   ├── data/
│   │   ├── BankRepository.kt           # Repository interface
│   │   ├── RemoteBankRepositoryImpl.kt  # Retrofit implementation
│   │   ├── TokenManager.kt             # JWT token persistence
│   │   └── api/
│   │       ├── BankApiService.kt        # Retrofit API endpoints
│   │       └── AuthInterceptor.kt       # Bearer token injection
│   ├── ui/
│   │   ├── auth/                        # Login & Register screens
│   │   ├── dashboard/                   # Dashboard with balance & chart
│   │   ├── cards/                       # Virtual card management
│   │   ├── transfer/                    # Transfer & QR scanner
│   │   ├── transaction/                 # Deposit & Withdraw
│   │   ├── history/                     # Transaction history
│   │   ├── profile/                     # User profile
│   │   └── components/                  # Shared UI components
│   └── theme/                           # Dark theme & colors
├── backend/
│   ├── cmd/server/main.go               # Server entry point
│   ├── internal/
│   │   ├── config/                      # Database config
│   │   ├── handlers/                    # API route handlers
│   │   ├── middleware/                   # Auth middleware
│   │   └── models/                      # GORM models
│   ├── Dockerfile
│   └── render.yaml                      # Render deployment config
└── e2e-tests/                           # WebdriverIO + Appium tests
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login |
| GET | `/api/user/balance` | Get balance |
| GET | `/api/user/profile` | Get user profile |
| POST | `/api/user/profile` | Update profile |
| POST | `/api/transactions/deposit` | Deposit funds |
| POST | `/api/transactions/withdraw` | Withdraw funds |
| POST | `/api/transactions/transfer` | Transfer to another user |
| GET | `/api/transactions/history` | Transaction history |
| GET | `/api/cards` | List cards |
| POST | `/api/cards` | Create card |
| PUT | `/api/cards/:id/freeze` | Toggle freeze card |
| PUT | `/api/cards/:id/limit` | Update card limit |
| PUT | `/api/cards/:id/edit` | Update card info |

## Navigation Routes

| Route | Screen |
|-------|--------|
| `login` | Login screen |
| `register` | Registration screen |
| `dashboard` | Main dashboard |
| `cards` | Card management |
| `transfer?recipient={}` | Transfer screen |
| `qr_scanner` | QR code scanner |
| `deposit` | Deposit screen |
| `withdraw` | Withdraw screen |
| `history` | Transaction history |
| `profile` | User profile |
