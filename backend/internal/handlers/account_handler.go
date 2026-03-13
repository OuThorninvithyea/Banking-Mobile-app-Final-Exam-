package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/middleware"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"github.com/yourusername/hustlebank-backend/internal/repository"
)

type AccountHandler struct {
	accountRepo repository.AccountRepository
}

func NewAccountHandler(accountRepo repository.AccountRepository) *AccountHandler {
	return &AccountHandler{accountRepo: accountRepo}
}

type createAccountRequest struct {
	Name string `json:"name" binding:"required"`
	Type string `json:"type" binding:"required"`
}

func (h *AccountHandler) GetAccounts(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	accounts, err := h.accountRepo.FindByUserID(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to fetch accounts"})
		return
	}

	c.JSON(http.StatusOK, accounts)
}

func (h *AccountHandler) CreateAccount(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	var req createAccountRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	validTypes := map[string]bool{
		"CURRENT":       true,
		"SAVINGS":       true,
		"FIXED_DEPOSIT": true,
		"BUSINESS":      true,
	}
	if !validTypes[req.Type] {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid account type"})
		return
	}

	account := &models.Account{
		UserID:        userID,
		AccountNumber: models.GenerateAccountNumber(),
		AccountName:   req.Name,
		Balance:       0.0,
		Type:          req.Type,
		Currency:      "USD",
	}

	if err := h.accountRepo.Create(account); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create account"})
		return
	}

	c.JSON(http.StatusCreated, account)
}
