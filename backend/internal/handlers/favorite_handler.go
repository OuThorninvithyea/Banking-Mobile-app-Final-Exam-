package handlers

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"
	"github.com/yourusername/hustlebank-backend/internal/middleware"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"github.com/yourusername/hustlebank-backend/internal/repository"
)

type FavoriteHandler struct {
	favoriteRepo repository.FavoriteRepository
}

func NewFavoriteHandler(favoriteRepo repository.FavoriteRepository) *FavoriteHandler {
	return &FavoriteHandler{favoriteRepo: favoriteRepo}
}

type addFavoriteRequest struct {
	Name          string `json:"name" binding:"required"`
	AccountNumber string `json:"account_number" binding:"required"`
}

func (h *FavoriteHandler) GetFavorites(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	favorites, err := h.favoriteRepo.FindByUserID(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to fetch favorites"})
		return
	}

	c.JSON(http.StatusOK, favorites)
}

func (h *FavoriteHandler) AddFavorite(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	var req addFavoriteRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	favorite := &models.Favorite{
		UserID:        userID,
		Name:          req.Name,
		AccountNumber: req.AccountNumber,
		BankName:      "HustleBank",
	}

	if err := h.favoriteRepo.Create(favorite); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to add favorite"})
		return
	}

	c.JSON(http.StatusCreated, favorite)
}

func (h *FavoriteHandler) RemoveFavorite(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	favoriteID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid favorite id"})
		return
	}

	if err := h.favoriteRepo.Delete(favoriteID, userID); err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"error": "favorite not found"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to remove favorite"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "favorite removed successfully"})
}
