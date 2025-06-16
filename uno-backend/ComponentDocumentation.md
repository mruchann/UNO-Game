# UNO Backend Component Documentation

This document provides detailed information about each major component in the UNO backend system.

## User Component

### Overview
The User component handles user registration, authentication, and profile management.

### Key Classes
- `User`: Entity class representing a user with ID, username, email, and password.
- `UserController`: REST controller handling user-related HTTP requests.
- `UserService`: Service class implementing user-related business logic.
- `UserRepository`: JPA repository for user data access.

### Endpoints
- POST `/users/register`: Register a new user
- POST `/users/login`: Authenticate a user and return JWT token
- GET `/users/{id}`: Get user information by ID
- PUT `/users/{id}`: Update user information
- DELETE `/users/{id}`: Delete a user

## Game Component

### Overview
The Game component is the core of the UNO game, handling game state, turns, and rule enforcement.

### Key Classes
- `Game`: Manages the game state, including players, cards, and turn management.
- `GameController`: REST controller exposing game-related endpoints.
- `SingleplayerGameService`: Implements singleplayer game logic, turn processing, and rule validation.
- `MultiplayerGameService`: Implements multiplayer game logic, turn processing, and rule validation.
- `Direction`: Enum defining game direction (CLOCKWISE or COUNTER_CLOCKWISE).

### Game Flow
1. Game initialization
   - Player setup
   - Card distribution
   - Initial card selection
2. Turn processing
   - Player action validation
   - Card play handling
   - Special card effects
3. Game completion
   - Winner determination
   - Score calculation
   - History update

### Game Rules Implementation
- Card play validation based on color and value matching
- Turn direction control via Reverse cards
- Skip turn implementation
- Draw Two and Wild Draw Four card stacking
- Wild card color selection
- UNO call when a player has one card left

## Card Component

### Overview
The Card component defines the different card types and their behaviors in the UNO game.

### Key Classes
- `Card`: Abstract base class for all cards.
- `NumberCard`: Cards with numerical values (0-9).
- `ActionCard`: Special action cards (Skip, Reverse, Draw Two).
- `WildCard`: Wild cards (Wild, Wild Draw Four).
- `CardColor`: Enum defining card colors (RED, BLUE, GREEN, YELLOW, NONE).
- `CardType`: Enum defining card types (NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR).

### Card Types
1. **Number Cards (0-9)**
   - Standard cards with values 0-9 in four colors.
   - Play matching color or number.

2. **Action Cards**
   - **Skip**: Skips the next player's turn.
   - **Reverse**: Reverses the direction of play.
   - **Draw Two**: Next player draws two cards and loses their turn.

3. **Wild Cards**
   - **Wild**: Player changes the current color.
   - **Wild Draw Four**: Player changes the current color and next player draws four cards.

## Player Component

### Overview
The Player component handles player actions and state management.

### Key Classes
- `Player`: Abstract base class for players, managing deck and actions.
- `HumanPlayer`: Implementation for human players.
- `ComputerPlayer`: AI implementation for computer-controlled players.

### Player Capabilities
- Drawing cards
- Playing cards
- Card selection (human vs. computer strategy)
- UNO calling

### Computer Player AI
- Simple strategy for card selection
- Color preference determination
- Special card usage decisions

## History Component

### Overview
The History component tracks and provides access to game results and statistics.

### Key Classes
- `GameRecord`: Entity storing game results.
- `GameHistoryController`: REST controller for accessing game history.
- `GameHistoryService`: Business logic for game history management.
- `GameHistoryRepository`: Data access for game history.

### Tracked Information
- Game participants
- Game duration
- Winner
- Player scores
- Special events

## Leaderboard Component

### Overview
The Leaderboard component manages player rankings and scores.

### Key Classes
- `LeaderboardRecord`: Entity storing player rankings.
- `LeaderboardController`: REST controller for leaderboard access.
- `LeaderboardService`: Business logic for leaderboard management.

### Ranking System
- Points awarded based on game outcomes
- Ranking calculation
- Season-based tracking

## Email Component

### Overview
The Email component handles email notifications for registration, verification, and game notifications.

### Key Classes
- Email service implementation
- Email templates

### Email Types
- Registration confirmation
- Account verification
- Password reset
- Game invitations
- Game result notifications

## Utility Components

### Overview
Various utility classes providing common functionality.

### Key Classes
- `CardUtil`: Utility methods for card operations.
- Exception handling classes
- Common DTOs and response objects

## Configuration

### Overview
Configuration classes for setting up the application.

### Key Classes
- `SecurityConfig`: Security configuration including password encoding.
- Application properties configuration 