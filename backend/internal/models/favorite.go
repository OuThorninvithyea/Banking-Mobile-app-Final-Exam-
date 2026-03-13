package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Favorite struct {
	ID            uuid.UUID `gorm:"type:uuid;primaryKey" json:"id"`
	UserID        uuid.UUID `gorm:"type:uuid;not null;index" json:"user_id"`
	Name          string    `gorm:"not null" json:"name"`
	AccountNumber string    `gorm:"not null" json:"account_number"`
	BankName      string    `gorm:"type:varchar(50);default:'HustleBank'" json:"bank_name"`
	CreatedAt     time.Time `json:"created_at"`
}

func (f *Favorite) BeforeCreate(tx *gorm.DB) error {
	if f.ID == uuid.Nil {
		f.ID = uuid.New()
	}
	return nil
}
