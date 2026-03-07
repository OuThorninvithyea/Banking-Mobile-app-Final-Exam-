package services

import (
	"errors"
	"fmt"
	"math/rand"
	"time"

	"github.com/yourusername/hustlebank-backend/internal/models"
	"github.com/yourusername/hustlebank-backend/internal/repository"
	"github.com/yourusername/hustlebank-backend/internal/utils"
)

type AuthService interface {
	Register(name, email, password string) (*models.User, string, error)
	Login(email, password string) (*models.User, string, error)
}

type authService struct {
	userRepo repository.UserRepository
}

func NewAuthService(userRepo repository.UserRepository) AuthService {
	return &authService{userRepo: userRepo}
}

func (s *authService) Register(name, email, password string) (*models.User, string, error) {
	existing, _ := s.userRepo.FindByEmail(email)
	if existing != nil {
		return nil, "", errors.New("email already registered")
	}

	hash, err := utils.HashPassword(password)
	if err != nil {
		return nil, "", fmt.Errorf("failed to hash password: %w", err)
	}

	user := &models.User{
		Name:          name,
		Email:         email,
		PasswordHash:  hash,
		AccountNumber: generateAccountNumber(),
		Balance:       1000.00, // welcome balance
	}

	if err := s.userRepo.Create(user); err != nil {
		return nil, "", fmt.Errorf("failed to create user: %w", err)
	}

	token, err := utils.GenerateToken(user.ID)
	if err != nil {
		return nil, "", fmt.Errorf("failed to generate token: %w", err)
	}

	return user, token, nil
}

func (s *authService) Login(email, password string) (*models.User, string, error) {
	user, err := s.userRepo.FindByEmail(email)
	if err != nil {
		return nil, "", errors.New("invalid email or password")
	}

	if !utils.CheckPasswordHash(password, user.PasswordHash) {
		return nil, "", errors.New("invalid email or password")
	}

	token, err := utils.GenerateToken(user.ID)
	if err != nil {
		return nil, "", fmt.Errorf("failed to generate token: %w", err)
	}

	return user, token, nil
}

func generateAccountNumber() string {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	return fmt.Sprintf("%03d-%03d-%04d", r.Intn(1000), r.Intn(1000), r.Intn(10000))
}
