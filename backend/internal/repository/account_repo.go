package repository

import (
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"gorm.io/gorm"
)

type AccountRepository interface {
	FindByUserID(userID uuid.UUID) ([]models.Account, error)
	FindByID(id uuid.UUID) (*models.Account, error)
	Create(account *models.Account) error
	Update(account *models.Account) error
	Delete(id uuid.UUID) error
}

type accountRepository struct {
	db *gorm.DB
}

func NewAccountRepository(db *gorm.DB) AccountRepository {
	return &accountRepository{db: db}
}

func (r *accountRepository) FindByUserID(userID uuid.UUID) ([]models.Account, error) {
	var accounts []models.Account
	if err := r.db.Where("user_id = ?", userID).Order("created_at asc").Find(&accounts).Error; err != nil {
		return nil, err
	}
	return accounts, nil
}

func (r *accountRepository) FindByID(id uuid.UUID) (*models.Account, error) {
	var account models.Account
	if err := r.db.First(&account, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &account, nil
}

func (r *accountRepository) Create(account *models.Account) error {
	return r.db.Create(account).Error
}

func (r *accountRepository) Update(account *models.Account) error {
	return r.db.Save(account).Error
}

func (r *accountRepository) Delete(id uuid.UUID) error {
	return r.db.Delete(&models.Account{}, "id = ?", id).Error
}
