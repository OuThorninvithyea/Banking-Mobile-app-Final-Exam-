package handlers

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/middleware"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"github.com/yourusername/hustlebank-backend/internal/repository"
)

type AccountHandler struct {
	accountRepo repository.AccountRepository
	userRepo    repository.UserRepository
}

func NewAccountHandler(accountRepo repository.AccountRepository, userRepo ...repository.UserRepository) *AccountHandler {
	h := &AccountHandler{accountRepo: accountRepo}
	if len(userRepo) > 0 {
		h.userRepo = userRepo[0]
	}
	return h
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

type editAccountRequest struct {
	Name string `json:"name"`
	Type string `json:"type"`
}

func (h *AccountHandler) EditAccount(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	accountID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid account id"})
		return
	}

	var req editAccountRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	account, err := h.accountRepo.FindByID(accountID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "account not found"})
		return
	}

	if account.UserID != userID {
		c.JSON(http.StatusForbidden, gin.H{"error": "not your account"})
		return
	}

	if req.Name != "" {
		account.AccountName = req.Name
	}
	if req.Type != "" {
		validTypes := map[string]bool{
			"CURRENT": true, "SAVINGS": true, "FIXED_DEPOSIT": true, "BUSINESS": true,
		}
		if !validTypes[req.Type] {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid account type"})
			return
		}
		account.Type = req.Type
	}

	if err := h.accountRepo.Update(account); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to update account"})
		return
	}

	c.JSON(http.StatusOK, account)
}

func (h *AccountHandler) DeleteAccount(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	accountID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid account id"})
		return
	}

	account, err := h.accountRepo.FindByID(accountID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "account not found"})
		return
	}

	if account.UserID != userID {
		c.JSON(http.StatusForbidden, gin.H{"error": "not your account"})
		return
	}

	if account.Balance > 0 {
		c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("please transfer the remaining $%.2f before deleting", account.Balance)})
		return
	}

	if err := h.accountRepo.Delete(accountID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to delete account"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Account deleted successfully"})
}

type accountTransferRequest struct {
	FromAccountID string  `json:"from_account_id" binding:"required"`
	ToAccountID   string  `json:"to_account_id" binding:"required"`
	Amount        float64 `json:"amount" binding:"required,gt=0"`
}

func (h *AccountHandler) TransferBetweenAccounts(c *gin.Context) {
	userID := c.MustGet(middleware.UserIDKey).(uuid.UUID)

	var req accountTransferRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	fromID, err := uuid.Parse(req.FromAccountID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid from_account_id"})
		return
	}
	toID, err := uuid.Parse(req.ToAccountID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid to_account_id"})
		return
	}

	if fromID == toID {
		c.JSON(http.StatusBadRequest, gin.H{"error": "cannot transfer to the same account"})
		return
	}

	isPrimaryFrom := fromID == userID
	isPrimaryTo := toID == userID

	var fromBalance float64
	var toBalance float64

	if isPrimaryFrom {
		user, err := h.userRepo.FindByID(userID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to load primary account"})
			return
		}
		fromBalance = user.Balance

		if req.Amount > fromBalance {
			c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("insufficient funds. Available: $%.2f", fromBalance)})
			return
		}

		if isPrimaryTo {
			c.JSON(http.StatusBadRequest, gin.H{"error": "cannot transfer to the same account"})
			return
		}

		toAcct, err := h.accountRepo.FindByID(toID)
		if err != nil || toAcct.UserID != userID {
			c.JSON(http.StatusBadRequest, gin.H{"error": "destination account not found"})
			return
		}

		user.Balance -= req.Amount
		toAcct.Balance += req.Amount

		if err := h.userRepo.Update(user); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to debit source"})
			return
		}
		if err := h.accountRepo.Update(toAcct); err != nil {
			user.Balance += req.Amount
			_ = h.userRepo.Update(user)
			c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to credit destination"})
			return
		}

		toBalance = toAcct.Balance
		fromBalance = user.Balance
	} else {
		fromAcct, err := h.accountRepo.FindByID(fromID)
		if err != nil || fromAcct.UserID != userID {
			c.JSON(http.StatusBadRequest, gin.H{"error": "source account not found"})
			return
		}

		if req.Amount > fromAcct.Balance {
			c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("insufficient funds. Available: $%.2f", fromAcct.Balance)})
			return
		}

		if isPrimaryTo {
			user, err := h.userRepo.FindByID(userID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to load primary account"})
				return
			}

			fromAcct.Balance -= req.Amount
			user.Balance += req.Amount

			if err := h.accountRepo.Update(fromAcct); err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to debit source"})
				return
			}
			if err := h.userRepo.Update(user); err != nil {
				fromAcct.Balance += req.Amount
				_ = h.accountRepo.Update(fromAcct)
				c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to credit destination"})
				return
			}

			fromBalance = fromAcct.Balance
			toBalance = user.Balance
		} else {
			toAcct, err := h.accountRepo.FindByID(toID)
			if err != nil || toAcct.UserID != userID {
				c.JSON(http.StatusBadRequest, gin.H{"error": "destination account not found"})
				return
			}

			fromAcct.Balance -= req.Amount
			toAcct.Balance += req.Amount

			if err := h.accountRepo.Update(fromAcct); err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to debit source"})
				return
			}
			if err := h.accountRepo.Update(toAcct); err != nil {
				fromAcct.Balance += req.Amount
				_ = h.accountRepo.Update(fromAcct)
				c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to credit destination"})
				return
			}

			fromBalance = fromAcct.Balance
			toBalance = toAcct.Balance
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"message":      "Transfer successful",
		"from_balance": fromBalance,
		"to_balance":   toBalance,
	})
}
