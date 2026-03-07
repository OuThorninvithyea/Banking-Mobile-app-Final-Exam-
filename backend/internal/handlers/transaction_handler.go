package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/middleware"
	"github.com/yourusername/hustlebank-backend/internal/services"
)

type TransactionHandler struct {
	txService services.TransactionService
}

func NewTransactionHandler(txService services.TransactionService) *TransactionHandler {
	return &TransactionHandler{txService: txService}
}

type depositRequest struct {
	Amount float64 `json:"amount" binding:"required,gt=0"`
}

type withdrawRequest struct {
	Amount float64 `json:"amount" binding:"required,gt=0"`
}

type transferRequest struct {
	Amount      float64   `json:"amount" binding:"required,gt=0"`
	RecipientID uuid.UUID `json:"recipient_id" binding:"required"`
}

func (h *TransactionHandler) GetBalance(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	balance, err := h.txService.GetBalance(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"balance": balance})
}

func (h *TransactionHandler) Deposit(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	var req depositRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if err := h.txService.Deposit(userID, req.Amount); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "deposit successful"})
}

func (h *TransactionHandler) Withdraw(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	var req withdrawRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if err := h.txService.Withdraw(userID, req.Amount); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "withdrawal successful"})
}

func (h *TransactionHandler) Transfer(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	var req transferRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if err := h.txService.Transfer(userID, req.RecipientID, req.Amount); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "transfer successful"})
}

func (h *TransactionHandler) GetHistory(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	transactions, err := h.txService.GetHistory(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"transactions": transactions})
}
