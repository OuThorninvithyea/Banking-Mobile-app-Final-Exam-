# Task: Setup Go Backend Architecture

## Context
This project is an Android mobile banking application. We are replacing the hardcoded `MockBankRepositoryImpl` data with a real, production-ready Go backend using Gin, PostgreSQL, GORM, JWT, and Bcrypt.

The Android app resides in the `app/` folder. The Go backend should be created at the root level alongside it in a new folder named `backend/`.

## Objective
Implement a robust RESTful API in Go that mirrors the HTTP endpoints defined in the Android app's `BankApiService.kt` interface. The Android app relies on the following endpoints:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/user/balance`
- `GET /api/user/profile`
- `POST /api/user/profile`
- `POST /api/transactions/transfer`
- `POST /api/transactions/deposit`
- `POST /api/transactions/withdraw`
- `GET /api/transactions/history`

## Action Required

Please generate the foundational structure and database models for the Go backend.

### Specific Instructions for Cursor
1. **Create the Folder Structure**  
   Create the following directory layout inside a new `backend/` folder:
   ```
   backend/
   ├── cmd/
   │   └── api/
   │       └── main.go           # Server setup & route definitions
   ├── internal/
   │   ├── config/
   │   │   └── database.go       # DB Connection logic via GORM
   │   ├── models/
   │   │   ├── user.go           # GORM Database schemas
   │   │   └── transaction.go
   │   ├── repository/
   │   │   ├── user_repo.go
   │   │   └── transaction_repo.go
   │   ├── services/
   │   │   ├── auth_service.go
   │   │   └── transaction_service.go
   │   ├── handlers/
   │   │   ├── auth_handler.go
   │   │   └── transaction_handler.go
   │   ├── middleware/
   │   │   └── jwt_middleware.go 
   │   └── utils/
   │       ├── jwt.go            
   │       └── hash.go           
   ├── .env                      # DB credentials & JWT key
   ├── go.mod                    
   └── go.sum                    
   ```
   *Initialize the Go module in `backend` by running `go mod init github.com/yourusername/hustlebank-backend` and fetching Gin, GORM (Postgres), JWT-go, Godotenv, and Bcrypt.*

2. **Define the GORM Database Models**  
   In `internal/models/user.go`, implement a `User` struct with fields for `ID` (UUID), `Name`, `Email`, `PasswordHash`, `AccountNumber`, `Balance` (Float64), and `Contact`.

   In `internal/models/transaction.go`, implement a `Transaction` struct with fields for `ID`, `UserID` (UUID linking to User table), `Type` (DEPOSIT, WITHDRAW, TRANSFER), `Amount`, and `RecipientID`.

3. **Establish DB Connection**  
   In `config/database.go`, use GORM and Postgres dialect to attempt a DB connection and auto-migrate the structs created in Step 2.

4. **Initialize Gin Router**  
   In `cmd/api/main.go`, set up a Gin engine, attach the `godotenv` loader, and define stub routes for all the endpoints listed in the Objective section so they are ready for the handler implementations.
