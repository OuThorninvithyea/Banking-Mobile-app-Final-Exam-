package models

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Account struct {
	ID            uuid.UUID `gorm:"type:uuid;primaryKey" json:"id"`
	UserID        uuid.UUID `gorm:"type:uuid;not null;index" json:"user_id"`
	AccountNumber string    `gorm:"uniqueIndex;not null" json:"account_number"`
	AccountName   string    `gorm:"not null" json:"account_name"`
	Balance       float64   `gorm:"default:0" json:"balance"`
	Type          string    `gorm:"type:varchar(20);not null" json:"type"`
	Currency      string    `gorm:"type:varchar(5);default:'USD'" json:"currency"`
	CreatedAt     time.Time `json:"created_at"`
}

func (a *Account) BeforeCreate(tx *gorm.DB) error {
	if a.ID == uuid.Nil {
		a.ID = uuid.New()
	}
	return nil
}

func GenerateAccountNumber() string {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	return fmt.Sprintf("%03d-%03d-%04d", r.Intn(1000), r.Intn(1000), r.Intn(10000))
}
