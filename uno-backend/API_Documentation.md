# UNO Backend API Documentation

This document provides detailed information about the REST API endpoints available in the UNO backend system. All API responses use JSON format.

## Base URL
```
https://ceng453-20242-group1-backend.onrender.com
```

## Authentication

Most API endpoints require authentication via JWT token. After login, include the token in all requests that require authentication:

```
Authorization: Bearer <token>
```

## User API

### Register a new user
```
POST /users/register
```

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com"
}
```

### Login
```
POST /users/login
```

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com"
  }
}
```

### Get User by ID
```
GET /users/{id}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com"
}
```

### Update User
```
PUT /users/{id}
```

**Request Body:**
```json
{
  "email": "newemail@example.com",
  "password": "newPassword123"
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "newemail@example.com"
}
```

### Delete User
```
DELETE /users/{id}
```

**Response (204 No Content)**

## Game API

### Create a new game
```
POST /games
```

**Request Body:**
```json
{
  "playerCount": 4,
  "computerPlayerCount": 3
}
```

**Response (201 Created):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "players": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "username": "john_doe",
      "type": "HUMAN"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "username": "Computer 1",
      "type": "COMPUTER"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440003",
      "username": "Computer 2",
      "type": "COMPUTER"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440004",
      "username": "Computer 3",
      "type": "COMPUTER"
    }
  ],
  "status": "INITIALIZED",
  "currentPlayer": 0
}
```

### Start Game
```
POST /games/{gameId}/start
```

**Response (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS",
  "currentPlayer": 0,
  "currentCard": {
    "color": "RED",
    "type": "NUMBER",
    "value": 5
  },
  "direction": "CLOCKWISE",
  "yourHand": [
    {
      "id": 1,
      "color": "RED",
      "type": "NUMBER",
      "value": 2
    },
    {
      "id": 2,
      "color": "BLUE",
      "type": "SKIP",
      "value": null
    }
  ],
  "playerCardCounts": [7, 7, 7, 7]
}
```

### Get Game State
```
GET /games/{gameId}
```

**Response (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS",
  "currentPlayer": 1,
  "currentCard": {
    "color": "BLUE",
    "type": "NUMBER",
    "value": 5
  },
  "direction": "CLOCKWISE",
  "yourHand": [
    {
      "id": 1,
      "color": "RED",
      "type": "NUMBER",
      "value": 2
    },
    {
      "id": 3,
      "color": "YELLOW",
      "type": "DRAW_TWO",
      "value": null
    }
  ],
  "playerCardCounts": [6, 7, 7, 7]
}
```

### Play a Card
```
POST /games/{gameId}/play
```

**Request Body:**
```json
{
  "cardId": 3,
  "declaredColor": "RED"  // Only needed for Wild cards
}
```

**Response (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS",
  "currentPlayer": 2,
  "currentCard": {
    "color": "YELLOW",
    "type": "DRAW_TWO",
    "value": null
  },
  "direction": "CLOCKWISE",
  "yourHand": [
    {
      "id": 1,
      "color": "RED",
      "type": "NUMBER",
      "value": 2
    }
  ],
  "playerCardCounts": [5, 9, 7, 7]
}
```

### Draw a Card
```
POST /games/{gameId}/draw
```

**Response (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS",
  "currentPlayer": 1,
  "currentCard": {
    "color": "BLUE",
    "type": "NUMBER",
    "value": 5
  },
  "direction": "CLOCKWISE",
  "yourHand": [
    {
      "id": 1,
      "color": "RED",
      "type": "NUMBER",
      "value": 2
    },
    {
      "id": 3,
      "color": "YELLOW",
      "type": "DRAW_TWO",
      "value": null
    },
    {
      "id": 4,
      "color": "GREEN",
      "type": "NUMBER",
      "value": 9
    }
  ],
  "playerCardCounts": [7, 7, 7, 7]
}
```

### Call UNO
```
POST /games/{gameId}/uno
```

**Response (200 OK):**
```json
{
  "status": "SUCCESS",
  "message": "UNO called successfully"
}
```

## Leaderboard API

### Get Global Leaderboard
```
GET /leaderboard
```

**Response (200 OK):**
```json
[
  {
    "username": "john_doe",
    "score": 1250,
    "gamesPlayed": 10,
    "gamesWon": 7
  },
  {
    "username": "jane_smith",
    "score": 980,
    "gamesPlayed": 8,
    "gamesWon": 5
  }
]
```

### Get User's Ranking
```
GET /leaderboard/user/{userId}
```

**Response (200 OK):**
```json
{
  "username": "john_doe",
  "rank": 1,
  "score": 1250,
  "gamesPlayed": 10,
  "gamesWon": 7
}
```

## Game History API

### Get User's Game History
```
GET /history/user/{userId}
```

**Response (200 OK):**
```json
[
  {
    "gameId": "550e8400-e29b-41d4-a716-446655440000",
    "date": "2024-05-01T15:30:00Z",
    "result": "WIN",
    "score": 150,
    "opponentUsernames": ["Computer 1", "Computer 2", "Computer 3"]
  },
  {
    "gameId": "550e8400-e29b-41d4-a716-446655440001",
    "date": "2024-05-02T18:45:00Z",
    "result": "LOSS",
    "score": 80,
    "opponentUsernames": ["jane_smith", "Computer 1", "Computer 2"]
  }
]
```

### Get Game Details
```
GET /history/game/{gameId}
```

**Response (200 OK):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "date": "2024-05-01T15:30:00Z",
  "duration": "00:15:32",
  "players": [
    {
      "username": "john_doe",
      "score": 150,
      "result": "WIN"
    },
    {
      "username": "Computer 1",
      "score": 45,
      "result": "LOSS"
    },
    {
      "username": "Computer 2",
      "score": 65,
      "result": "LOSS"
    },
    {
      "username": "Computer 3",
      "score": 20,
      "result": "LOSS"
    }
  ],
  "events": [
    {
      "time": "00:00:05",
      "player": "john_doe",
      "action": "PLAY_CARD",
      "card": "RED 5"
    },
    {
      "time": "00:00:12",
      "player": "Computer 1",
      "action": "PLAY_CARD",
      "card": "RED SKIP"
    }
  ]
}
```

## Error Responses

All API endpoints use standard HTTP status codes. In case of errors, the response body will contain error details:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input parameters",
  "timestamp": "2024-05-01T15:30:00Z",
  "path": "/users/register"
}
```

Common error status codes:
- 400 Bad Request: Invalid input parameters
- 401 Unauthorized: Missing or invalid authentication
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 409 Conflict: Resource already exists
- 500 Internal Server Error: Server-side error 