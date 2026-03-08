package repository

import (
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"gorm.io/gorm"
)

type CardRepository interface {
	Create(card *models.Card) error
	FindByUserID(userID uuid.UUID) ([]models.Card, error)
	FindByID(id uuid.UUID) (*models.Card, error)
	Update(card *models.Card) error
}

type cardRepository struct {
	db *gorm.DB
}

func NewCardRepository(db *gorm.DB) CardRepository {
	return &cardRepository{db: db}
}

func (r *cardRepository) Create(card *models.Card) error {
	return r.db.Create(card).Error
}

func (r *cardRepository) FindByUserID(userID uuid.UUID) ([]models.Card, error) {
	var cards []models.Card
	if err := r.db.Where("user_id = ?", userID).Order("created_at desc").Find(&cards).Error; err != nil {
		return nil, err
	}
	return cards, nil
}

func (r *cardRepository) FindByID(id uuid.UUID) (*models.Card, error) {
	var card models.Card
	if err := r.db.First(&card, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &card, nil
}

func (r *cardRepository) Update(card *models.Card) error {
	return r.db.Save(card).Error
}
