package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type TransactionType string

const (
	TransactionDeposit  TransactionType = "DEPOSIT"
	TransactionWithdraw TransactionType = "WITHDRAW"
	TransactionTransfer TransactionType = "TRANSFER"
)

type Transaction struct {
	ID          uuid.UUID       `gorm:"type:uuid;primaryKey" json:"id"`
	UserID      uuid.UUID       `gorm:"type:uuid;not null;index" json:"user_id"`
	User        User            `gorm:"foreignKey:UserID" json:"-"`
	Type        TransactionType `gorm:"type:varchar(10);not null" json:"type"`
	Amount      float64         `gorm:"not null" json:"amount"`
	RecipientID *uuid.UUID      `gorm:"type:uuid" json:"recipient_id,omitempty"`
	Timestamp   time.Time       `json:"timestamp"`
	CreatedAt   time.Time       `json:"created_at"`
}

func (t *Transaction) BeforeCreate(tx *gorm.DB) error {
	if t.ID == uuid.Nil {
		t.ID = uuid.New()
	}
	if t.Timestamp.IsZero() {
		t.Timestamp = time.Now()
	}
	return nil
}
