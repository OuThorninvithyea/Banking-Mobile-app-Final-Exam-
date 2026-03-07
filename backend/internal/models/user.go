package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type User struct {
	ID            uuid.UUID `gorm:"type:uuid;primaryKey" json:"id"`
	Name          string    `gorm:"not null" json:"name"`
	Email         string    `gorm:"uniqueIndex;not null" json:"email"`
	PasswordHash  string    `gorm:"not null" json:"-"`
	AccountNumber string    `gorm:"uniqueIndex;not null" json:"account_number"`
	Balance       float64   `gorm:"default:0" json:"balance"`
	Contact       string    `json:"contact"`
	CreatedAt     time.Time `json:"created_at"`
	UpdatedAt     time.Time `json:"updated_at"`
}

func (u *User) BeforeCreate(tx *gorm.DB) error {
	if u.ID == uuid.Nil {
		u.ID = uuid.New()
	}
	return nil
}
