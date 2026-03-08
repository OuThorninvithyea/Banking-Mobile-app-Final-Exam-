# Card Functionality Implementation Plan

Currently, the `Cards` screen is a static aesthetic UI with no backend connection. To make "all functionality in card working," we need to implement a full-stack feature for managing user debit/credit cards.

## 1. Backend Implementation (Go)

### A. Database Models (`backend/internal/models/card.go`)
Create a new `Card` struct:
```go
type Card struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey" json:"id"`
	UserID    uuid.UUID `gorm:"type:uuid;not null;index" json:"user_id"`
	Number    string    `gorm:"type:varchar(16);unique;not null" json:"number"`
	Expiry    string    `gorm:"type:varchar(5);not null" json:"expiry"`
	CVV       string    `gorm:"type:varchar(4);not null" json:"cvv"`
	IsFrozen  bool      `gorm:"default:false" json:"is_frozen"`
	Type      string    `gorm:"type:varchar(20);not null" json:"type"` // e.g., "Virtual", "Physical"
	Limit     float64   `gorm:"default:10000.0" json:"limit"`
	CreatedAt time.Time `json:"created_at"`
}
```

### B. Route Controllers (`backend/internal/api/handlers/card.go`)
1.  **`CreateCard(c *gin.Context)`**: Generates a new random 16-digit card number, CVV, and expiry date. Links it to the authenticated user's `uuid`.
2.  **`GetCards(c *gin.Context)`**: Retrieves all cards owned by the authenticated user.
3.  **`ToggleFreezeCard(c *gin.Context)`**: Flips the `IsFrozen` boolean on a specific card ID.
4.  **`UpdateCardLimit(c *gin.Context)`**: Updates the spending `Limit` for a specific card.
5.  **`UpdateCardInfo(c *gin.Context)`**: Edits card information such as the card's `Type` or a custom label/name if added in the future.

### C. Router (`backend/cmd/api/main.go`)
Add the new card routes under the authenticated router group:
```go
api.POST("/cards", handlers.CreateCard)
api.GET("/cards", handlers.GetCards)
api.PUT("/cards/:id/freeze", handlers.ToggleFreezeCard)
api.PUT("/cards/:id/limit", handlers.UpdateCardLimit)
api.PUT("/cards/:id/edit", handlers.UpdateCardInfo)
```

## 2. Frontend Models & API (Android)

### A. Data Models (`app/src/main/java/com/hustle/bankapp/data/Card.kt`)
Create data classes matching the Go backend:
```kotlin
data class Card(
    val id: String,
    val number: String,
    val expiry: String,
    val cvv: String,
    @SerializedName("is_frozen") val isFrozen: Boolean,
    val type: String,
    val limit: Double
)
```

### B. Retrofit Interface (`app/src/main/java/com/hustle/bankapp/data/api/BankApiService.kt`)
Add the endpoints:
```kotlin
@GET("/api/cards")
suspend fun getCards(): Response<List<Card>>

@POST("/api/cards")
suspend fun createCard(): Response<Card>

@PUT("/api/cards/{id}/freeze")
suspend fun toggleFreezeCard(@Path("id") cardId: String): Response<Card>

@PUT("/api/cards/{id}/limit")
suspend fun updateCardLimit(@Path("id") cardId: String, @Body request: LimitRequest): Response<Card>

@PUT("/api/cards/{id}/edit")
suspend fun updateCardInfo(@Path("id") cardId: String, @Body request: EditCardRequest): Response<Card>
```

### C. Repository Updates
Add these new functions to `BankRepository` and implement them in `RemoteBankRepositoryImpl`. Provide a Flow or standard Suspend functions to fetch the active cards.

## 3. UI Implementation (Android Jetpack Compose)

### A. Create `CardsViewModel`
Build a new `CardsViewModel` that holds `uiState: StateFlow<CardsUiState>`.
-   Load the list of cards on init.
-   Provide functions to handle the "Freeze", "Block", "Limits", "Replace", and "Edit" button clicks via Repository logic.
-   Track `selectedCardIndex` in the state so the quick actions apply only to the currently swiped card.

### B. Update `CardsScreen.kt`
-   Pass the new `CardsViewModel` instead of relying purely on the general `BankRepository`.
-   **Multi-Card Swipe**: Re-implement `PrimaryCard` using Compose Foundation's `HorizontalPager`.
    -   Bind the pager to the list of fetched `Card` objects from the backend.
    -   When a user swipes left or right, trigger the ViewModel to update the `selectedCardIndex` and fetch the respective transactions for that specific card if needed.
-   **Edit Info**: Add an "Edit Card" function. Create an `AlertDialog` or BottomSheet to allow modifying the `Type` or settings.
-   Update the "Available", "Card Limit", and Quick Action controls to dynamically display and modify the active, centered card's data.
