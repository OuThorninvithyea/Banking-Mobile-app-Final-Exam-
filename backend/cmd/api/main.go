package main

import (
	"log"
	"os"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"github.com/yourusername/hustlebank-backend/internal/config"
	"github.com/yourusername/hustlebank-backend/internal/handlers"
	"github.com/yourusername/hustlebank-backend/internal/middleware"
	"github.com/yourusername/hustlebank-backend/internal/repository"
	"github.com/yourusername/hustlebank-backend/internal/services"
)

func main() {
	// Load environment variables from .env
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, reading from environment")
	}

	// Connect to Postgres and run AutoMigrate
	config.ConnectDatabase()

	// Wire up repositories, services, handlers
	userRepo := repository.NewUserRepository(config.DB)
	txRepo := repository.NewTransactionRepository(config.DB)
	cardRepo := repository.NewCardRepository(config.DB)
	accountRepo := repository.NewAccountRepository(config.DB)
	favoriteRepo := repository.NewFavoriteRepository(config.DB)

	authService := services.NewAuthService(userRepo)
	txService := services.NewTransactionService(userRepo, txRepo)

	authHandler := handlers.NewAuthHandler(authService, userRepo)
	txHandler := handlers.NewTransactionHandler(txService)
	cardHandler := handlers.NewCardHandlerFull(cardRepo, accountRepo, userRepo)
	accountHandler := handlers.NewAccountHandler(accountRepo, userRepo)
	favoriteHandler := handlers.NewFavoriteHandler(favoriteRepo)

	// Gin engine
	r := gin.Default()

	// ── Public routes ───────────────────────────────────────────────
	auth := r.Group("/api/auth")
	{
		auth.POST("/register", authHandler.Register)
		auth.POST("/login", authHandler.Login)
	}

	// ── Protected routes (JWT required) ─────────────────────────────
	protected := r.Group("/api")
	protected.Use(middleware.AuthRequired())
	{
		// User
		protected.GET("/user/balance", txHandler.GetBalance)
		protected.GET("/user/profile", authHandler.GetProfile)
		protected.POST("/user/profile", authHandler.UpdateProfile)

		// Transactions
		protected.POST("/transactions/deposit", txHandler.Deposit)
		protected.POST("/transactions/withdraw", txHandler.Withdraw)
		protected.POST("/transactions/transfer", txHandler.Transfer)
		protected.GET("/transactions/history", txHandler.GetHistory)

		// Cards
		protected.POST("/cards", cardHandler.CreateCard)
		protected.GET("/cards", cardHandler.GetCards)
		protected.PUT("/cards/:id/freeze", cardHandler.ToggleFreezeCard)
		protected.PUT("/cards/:id/limit", cardHandler.UpdateCardLimit)
		protected.PUT("/cards/:id/edit", cardHandler.UpdateCardInfo)
		protected.PUT("/cards/:id/link", cardHandler.LinkCardToAccount)

		// Accounts
		protected.GET("/accounts", accountHandler.GetAccounts)
		protected.POST("/accounts", accountHandler.CreateAccount)
		protected.PUT("/accounts/:id", accountHandler.EditAccount)
		protected.DELETE("/accounts/:id", accountHandler.DeleteAccount)
		protected.POST("/accounts/transfer", accountHandler.TransferBetweenAccounts)

		// Favorites
		protected.GET("/favorites", favoriteHandler.GetFavorites)
		protected.POST("/favorites", favoriteHandler.AddFavorite)
		protected.DELETE("/favorites/:id", favoriteHandler.RemoveFavorite)
	}

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Printf("HustleBank API listening on :%s", port)
	if err := r.Run(":" + port); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
