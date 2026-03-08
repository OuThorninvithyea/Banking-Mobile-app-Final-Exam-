package handlers

import (
	"fmt"
	"math/rand"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/middleware"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"github.com/yourusername/hustlebank-backend/internal/repository"
)

type CardHandler struct {
	cardRepo repository.CardRepository
}

func NewCardHandler(cardRepo repository.CardRepository) *CardHandler {
	return &CardHandler{cardRepo: cardRepo}
}

type updateLimitRequest struct {
	Limit float64 `json:"limit" binding:"required,gt=0"`
}

type updateCardInfoRequest struct {
	Type string `json:"type" binding:"required"`
}

func (h *CardHandler) CreateCard(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	rng := rand.New(rand.NewSource(time.Now().UnixNano()))
	number := fmt.Sprintf("%016d", rng.Int63n(9_999_999_999_999_999))
	cvv := fmt.Sprintf("%03d", rng.Intn(999)+1)
	expiry := time.Now().AddDate(3, 0, 0).Format("01/06")

	card := &models.Card{
		UserID:   userID,
		Number:   number,
		Expiry:   expiry,
		CVV:      cvv,
		IsFrozen: false,
		Type:     "Virtual",
		Limit:    10000.0,
	}

	if err := h.cardRepo.Create(card); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create card"})
		return
	}

	c.JSON(http.StatusCreated, card)
}

func (h *CardHandler) GetCards(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	cards, err := h.cardRepo.FindByUserID(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to fetch cards"})
		return
	}

	c.JSON(http.StatusOK, cards)
}

func (h *CardHandler) ToggleFreezeCard(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	cardID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid card id"})
		return
	}

	card, err := h.cardRepo.FindByID(cardID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "card not found"})
		return
	}

	if card.UserID != userID {
		c.JSON(http.StatusForbidden, gin.H{"error": "not your card"})
		return
	}

	card.IsFrozen = !card.IsFrozen
	if err := h.cardRepo.Update(card); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to update card"})
		return
	}

	c.JSON(http.StatusOK, card)
}

func (h *CardHandler) UpdateCardLimit(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	cardID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid card id"})
		return
	}

	var req updateLimitRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	card, err := h.cardRepo.FindByID(cardID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "card not found"})
		return
	}

	if card.UserID != userID {
		c.JSON(http.StatusForbidden, gin.H{"error": "not your card"})
		return
	}

	card.Limit = req.Limit
	if err := h.cardRepo.Update(card); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to update card limit"})
		return
	}

	c.JSON(http.StatusOK, card)
}

func (h *CardHandler) UpdateCardInfo(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	cardID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid card id"})
		return
	}

	var req updateCardInfoRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	card, err := h.cardRepo.FindByID(cardID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "card not found"})
		return
	}

	if card.UserID != userID {
		c.JSON(http.StatusForbidden, gin.H{"error": "not your card"})
		return
	}

	card.Type = req.Type
	if err := h.cardRepo.Update(card); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to update card info"})
		return
	}

	c.JSON(http.StatusOK, card)
}
