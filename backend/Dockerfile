# ── Build stage ──────────────────────────────────────────────────────
FROM golang:1.25-alpine AS builder

RUN apk add --no-cache git

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -o /hustlebank-api ./cmd/api

# ── Runtime stage ────────────────────────────────────────────────────
FROM alpine:3.20

RUN apk add --no-cache ca-certificates

WORKDIR /app

COPY --from=builder /hustlebank-api .

EXPOSE 8080

ENV GIN_MODE=release

CMD ["./hustlebank-api"]
