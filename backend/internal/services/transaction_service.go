package services

import (
	"errors"
	"fmt"

	"github.com/google/uuid"
	"github.com/yourusername/hustlebank-backend/internal/models"
	"github.com/yourusername/hustlebank-backend/internal/repository"
)

type TransactionService interface {
	Deposit(userID uuid.UUID, amount float64) error
	Withdraw(userID uuid.UUID, amount float64) error
	Transfer(senderID uuid.UUID, recipientID uuid.UUID, amount float64) error
	GetHistory(userID uuid.UUID) ([]models.Transaction, error)
	GetBalance(userID uuid.UUID) (float64, error)
}

type transactionService struct {
	userRepo repository.UserRepository
	txRepo   repository.TransactionRepository
}

func NewTransactionService(userRepo repository.UserRepository, txRepo repository.TransactionRepository) TransactionService {
	return &transactionService{userRepo: userRepo, txRepo: txRepo}
}

func (s *transactionService) GetBalance(userID uuid.UUID) (float64, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		return 0, errors.New("user not found")
	}
	return user.Balance, nil
}

func (s *transactionService) Deposit(userID uuid.UUID, amount float64) error {
	if amount <= 0 {
		return errors.New("deposit amount must be positive")
	}

	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		return errors.New("user not found")
	}

	user.Balance += amount
	if err := s.userRepo.Update(user); err != nil {
		return fmt.Errorf("failed to update balance: %w", err)
	}

	tx := &models.Transaction{
		UserID: userID,
		Type:   models.TransactionDeposit,
		Amount: amount,
	}
	return s.txRepo.Create(tx)
}

func (s *transactionService) Withdraw(userID uuid.UUID, amount float64) error {
	if amount <= 0 {
		return errors.New("withdrawal amount must be positive")
	}

	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		return errors.New("user not found")
	}

	if user.Balance < amount {
		return errors.New("insufficient funds")
	}

	user.Balance -= amount
	if err := s.userRepo.Update(user); err != nil {
		return fmt.Errorf("failed to update balance: %w", err)
	}

	tx := &models.Transaction{
		UserID: userID,
		Type:   models.TransactionWithdraw,
		Amount: amount,
	}
	return s.txRepo.Create(tx)
}

func (s *transactionService) Transfer(senderID uuid.UUID, recipientID uuid.UUID, amount float64) error {
	if amount <= 0 {
		return errors.New("transfer amount must be positive")
	}

	sender, err := s.userRepo.FindByID(senderID)
	if err != nil {
		return errors.New("sender not found")
	}

	recipient, err := s.userRepo.FindByID(recipientID)
	if err != nil {
		return errors.New("recipient not found")
	}

	if sender.Balance < amount {
		return errors.New("insufficient funds")
	}

	sender.Balance -= amount
	recipient.Balance += amount

	if err := s.userRepo.Update(sender); err != nil {
		return fmt.Errorf("failed to debit sender: %w", err)
	}
	if err := s.userRepo.Update(recipient); err != nil {
		// Attempt to roll back sender's balance
		sender.Balance += amount
		_ = s.userRepo.Update(sender)
		return fmt.Errorf("failed to credit recipient: %w", err)
	}

	tx := &models.Transaction{
		UserID:      senderID,
		Type:        models.TransactionTransfer,
		Amount:      amount,
		RecipientID: &recipientID,
	}
	return s.txRepo.Create(tx)
}

func (s *transactionService) GetHistory(userID uuid.UUID) ([]models.Transaction, error) {
	return s.txRepo.FindByUserID(userID)
}
