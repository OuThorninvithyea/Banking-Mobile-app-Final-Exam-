package repository

import (
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"gorm.io/gorm"
)

type FavoriteRepository interface {
	FindByUserID(userID uuid.UUID) ([]models.Favorite, error)
	Create(favorite *models.Favorite) error
	Delete(id uuid.UUID, userID uuid.UUID) error
}

type favoriteRepository struct {
	db *gorm.DB
}

func NewFavoriteRepository(db *gorm.DB) FavoriteRepository {
	return &favoriteRepository{db: db}
}

func (r *favoriteRepository) FindByUserID(userID uuid.UUID) ([]models.Favorite, error) {
	var favorites []models.Favorite
	if err := r.db.Where("user_id = ?", userID).Order("created_at asc").Find(&favorites).Error; err != nil {
		return nil, err
	}
	return favorites, nil
}

func (r *favoriteRepository) Create(favorite *models.Favorite) error {
	return r.db.Create(favorite).Error
}

func (r *favoriteRepository) Delete(id uuid.UUID, userID uuid.UUID) error {
	result := r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Favorite{})
	if result.Error != nil {
		return result.Error
	}
	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}
