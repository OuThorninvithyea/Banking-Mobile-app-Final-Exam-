package repository

import (
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"gorm.io/gorm"
)

type TransactionRepository interface {
	Create(tx *models.Transaction) error
	FindByUserID(userID uuid.UUID) ([]models.Transaction, error)
}

type transactionRepository struct {
	db *gorm.DB
}

func NewTransactionRepository(db *gorm.DB) TransactionRepository {
	return &transactionRepository{db: db}
}

func (r *transactionRepository) Create(tx *models.Transaction) error {
	return r.db.Create(tx).Error
}

func (r *transactionRepository) FindByUserID(userID uuid.UUID) ([]models.Transaction, error) {
	var transactions []models.Transaction
	if err := r.db.Where("user_id = ?", userID).Order("timestamp desc").Find(&transactions).Error; err != nil {
		return nil, err
	}
	return transactions, nil
}
