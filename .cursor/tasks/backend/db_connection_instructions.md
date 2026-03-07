# Task: Go Backend Database Connection

## Context
We are implementing the Go backend for the Hustle Bank application. The initial structure has been scaffolded. The next step is to install dependencies, configure the PostgreSQL connection to Neon using GORM, and initialize it when the server starts.

## Action Required

Please execute the following steps to establish the database connection.

### Specific Instructions for Cursor
1. **Install Required Go Packages**
   Open the terminal in the `backend/` folder and run:
   ```bash
   go get gorm.io/gorm
   go get gorm.io/driver/postgres
   go get github.com/joho/godotenv
   ```

2. **Write the Database Connection Code**
   Create a new file `backend/internal/config/database.go` (or `db.go`) and add the following connection logic:
   ```go
   package config

   import (
   	"log"
   	"os"

   	"github.com/joho/godotenv"
   	"gorm.io/driver/postgres"
   	"gorm.io/gorm"
   )

   // DB is the global variable we will use to access the database across our API
   var DB *gorm.DB

   func ConnectDatabase() {
   	// 1. Load the .env file (Only needed for local development)
   	err := godotenv.Load()
   	if err != nil {
   		log.Println("Note: No .env file found. Falling back to system environment variables (this is normal for Render).")
   	}

   	// 2. Get the connection string from the environment
   	dsn := os.Getenv("DATABASE_URL")
   	if dsn == "" {
   		log.Fatal("Error: DATABASE_URL environment variable is missing!")
   	}

   	// 3. Connect to PostgreSQL using GORM
   	database, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
   	if err != nil {
   		log.Fatal("Failed to connect to Neon database! \n", err)
   	}

   	log.Println("Successfully connected to the HustleBank Database on Neon!")
   	
   	// Assign it to our global variable
   	DB = database
   }
   ```

3. **Call Database Init in `main.go`**
   Update `backend/cmd/api/main.go` to initialize the database connection before starting the server:
   ```go
   package main

   import (
   	"fmt"
   	"log"
   	"net/http"
   	"os"

   	"hustlebank-backend/internal/config" // Update with your actual go.mod module name
   	"github.com/gin-gonic/gin"
   )

   func main() {
   	// Connect to Neon Database
   	config.ConnectDatabase()

   	// Set up the Gin router
   	router := gin.Default()

   	router.GET("/ping", func(c *gin.Context) {
   		c.JSON(http.StatusOK, gin.H{
   			"message": "HustleBank API is live and connected to the database!",
   		})
   	})

   	// Start the server
   	port := os.Getenv("PORT")
   	if port == "" {
   		port = "8080"
   	}
   	
   	fmt.Println("Server running on port " + port)
   	router.Run(":" + port)
   }
   ```

4. **Environment Variables**
   Ensure you create a `.env` file in the `backend/` directory with the `DATABASE_URL` holding your Neon PostgreSQL connection string. Ensure `.env` is added to `.gitignore`.
