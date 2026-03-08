package config

import (
	"log"
	"os"

	"github.com/yourusername/hustlebank-backend/internal/models"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

func ConnectDatabase() {
	dsn := os.Getenv("DATABASE_URL")
	if dsn == "" {
		log.Fatal("DATABASE_URL environment variable is not set")
	}

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
	})
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	log.Println("Database connection established")

	if err := db.AutoMigrate(&models.User{}, &models.Transaction{}, &models.Card{}); err != nil {
		log.Fatalf("AutoMigrate failed: %v", err)
	}

	log.Println("Database migrated successfully")
	DB = db
}
