package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Card struct {
	ID              uuid.UUID  `gorm:"type:uuid;primaryKey" json:"id"`
	UserID          uuid.UUID  `gorm:"type:uuid;not null;index" json:"user_id"`
	Number          string     `gorm:"type:varchar(16);unique;not null" json:"number"`
	Expiry          string     `gorm:"type:varchar(5);not null" json:"expiry"`
	CVV             string     `gorm:"type:varchar(4);not null" json:"cvv"`
	IsFrozen        bool       `gorm:"default:false" json:"is_frozen"`
	Type            string     `gorm:"type:varchar(20);not null" json:"type"`
	Limit           float64    `gorm:"default:10000.0" json:"limit"`
	LinkedAccountID *uuid.UUID `gorm:"type:uuid" json:"linked_account_id,omitempty"`
	LinkedAccountName string   `gorm:"-" json:"linked_account_name,omitempty"`
	CreatedAt       time.Time  `json:"created_at"`
}

func (c *Card) BeforeCreate(tx *gorm.DB) error {
	if c.ID == uuid.Nil {
		c.ID = uuid.New()
	}
	return nil
}
