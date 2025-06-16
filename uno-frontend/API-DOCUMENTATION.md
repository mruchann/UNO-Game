# UNO Game Frontend - API Documentation

This document describes the REST API endpoints that the UNO game frontend communicates with on the backend server.

## Base URL

By default, the API base URL is configured as:

```
http://localhost:8080
```

This can be changed in the `application.properties` file by modifying the `api.base-url` property.

## API Endpoints

### Start Game

Initializes a new game session with one human player and the specified number of computer players.

- **URL**: `/api/game/start`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**Request Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| username | String | The username for the human player |
| computerPlayerAmount | Integer | The number of computer players to add to the game (1-3) |

**Response**:

Returns a `GameStateDTO` object containing the initial game state.

**Example Request**:
```
POST /api/game/start
Content-Type: application/x-www-form-urlencoded

username=Player1&computerPlayerAmount=3
```

**Example Response**:
```json
{
  "players": [
    {
      "username": "Player1",
      "deck": [
        {
          "type": "NUMBER",
          "color": "RED",
          "number": 5
        },
        {
          "type": "SKIP",
          "color": "BLUE",
          "number": null
        }
        // More cards...
      ]
    },
    // More players...
  ],
  "currentPlayerNo": 0,
  "lastPlayedCard": {
    "type": "NUMBER",
    "color": "GREEN",
    "number": 9
  },
  "currentColor": "GREEN",
  "direction": "CLOCKWISE",
  "drawDeckSize": 40,
  "gameFinished": false,
  "winner": null
}
```

### Play Card (Human)

Plays a card from the human player's hand.

- **URL**: `/api/game/play-card-human`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**Request Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| playerId | Integer | The ID of the player (always 0 for human player) |
| cardIndex | Integer | The index of the card in the player's hand |
| cardColor | String | (Optional) The chosen color when playing a wild card. Required only for WILD and WILD_DRAW_FOUR cards. |

**Response**:

Returns a `GameStateDTO` object reflecting the updated game state after the card is played.

**Example Request**:
```
POST /api/game/play-card-human
Content-Type: application/x-www-form-urlencoded

playerId=0&cardIndex=3
```

**Example Request (Wild Card)**:
```
POST /api/game/play-card-human
Content-Type: application/x-www-form-urlencoded

playerId=0&cardIndex=2&cardColor=RED
```

### Play Card (Computer)

Triggers a computer player to play a card.

- **URL**: `/api/game/play-card-computer`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**Request Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| playerId | Integer | The ID of the computer player (1-3) |

**Response**:

Returns a `GameStateDTO` object reflecting the updated game state after the computer player makes a move.

**Example Request**:
```
POST /api/game/play-card-computer
Content-Type: application/x-www-form-urlencoded

playerId=1
```

### Draw Card

Draws a card from the deck and adds it to the specified player's hand.

- **URL**: `/api/game/draw-card`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**Request Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| playerId | Integer | The ID of the player drawing the card |

**Response**:

Returns a `GameStateDTO` object reflecting the updated game state after drawing a card.

**Example Request**:
```
POST /api/game/draw-card
Content-Type: application/x-www-form-urlencoded

playerId=0
```

### Use Cheat Card

Activates a cheat card effect for the human player.

- **URL**: `/api/game/cheat-card`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**Request Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| playerId | Integer | The ID of the player using the cheat (always 0 for human) |
| cheatType | String | The type of cheat effect to use (REVERSE, SKIP, DRAW_TWO, WILD, WILD_DRAW_FOUR) |

**Response**:

Returns a `GameStateDTO` object reflecting the updated game state after using the cheat.

**Example Request**:
```
POST /api/game/cheat-card
Content-Type: application/x-www-form-urlencoded

playerId=0&cheatType=SKIP
```

## Data Models

### GameStateDTO

Represents the complete state of the game.

| Field | Type | Description |
|-------|------|-------------|
| players | List<PlayerDTO> | List of players in the game |
| currentPlayerNo | Integer | Index of the current player's turn |
| lastPlayedCard | CardDTO | The most recently played card |
| currentColor | String | The active color in play |
| direction | String | Direction of play (CLOCKWISE or COUNTER_CLOCKWISE) |
| drawDeckSize | Integer | Number of cards remaining in the draw pile |
| gameFinished | Boolean | Whether the game has ended |
| winner | String | Username of the winner, if game is finished |

### PlayerDTO

Represents a player in the game.

| Field | Type | Description |
|-------|------|-------------|
| username | String | The player's name |
| deck | List<CardDTO> | List of cards in the player's hand |

### CardDTO

Represents a single UNO card.

| Field | Type | Description |
|-------|------|-------------|
| type | String | The card type (NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR) |
| color | String | The card color (RED, BLUE, GREEN, YELLOW, or null for wild cards) |
| number | Integer | The card number (for number cards) or null for action cards |

## Error Handling

When an API request fails, the backend returns an appropriate HTTP status code along with an error message. The frontend should handle these errors and display appropriate messages to the user.

Common error scenarios:
- 400 Bad Request: Invalid parameters or illegal move
- 401 Unauthorized: Authentication required
- 404 Not Found: Resource not found
- 500 Internal Server Error: Server-side error

## Rate Limiting

There are no specific rate limits implemented for these API endpoints, but clients should avoid making excessive requests to prevent overloading the server.

## Security Considerations

The API currently does not implement authentication or authorization. In a production environment, these endpoints should be secured using appropriate authentication mechanisms.

## API Versioning

The current API does not include version information in the URL. Future versions of the API may include version prefixes (e.g., `/api/v2/game/start`).

## Testing the API

You can test these API endpoints using tools like:
- cURL
- Postman
- HTTPie

Example cURL command:
```bash
curl -X POST \
  http://localhost:8080/api/game/start \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=Player1&computerPlayerAmount=3'
``` 