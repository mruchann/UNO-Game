# UNO Frontend - Technical Documentation

This document provides technical details about the UNO Frontend application's architecture, implementation, and guidelines for developers who want to maintain or extend the application.

## Architecture

### Overview

The UNO Frontend follows a client-server architecture where:
- The frontend (this application) is built with JavaFX and Spring Boot
- The backend server implements the game logic and state management
- Communication happens via RESTful HTTP requests

### Application Structure

```
src/main/java/tr/edu/metu/ceng/uno/
├── config/               # Configuration classes
│   └── AppConfig.java    # Defines beans like RestTemplate
├── model/                # Data Transfer Objects
│   ├── CardDTO.java      # Represents a card
│   ├── GameStateDTO.java # Represents the entire game state
│   └── PlayerDTO.java    # Represents a player
├── service/              # Services for business logic
│   └── GameService.java  # Handles API communication
├── view/                 # Custom UI components
│   └── CardImageView.java # Extended ImageView for cards
├── JavaFXApplication.java # JavaFX application class
├── MainViewController.java # Main controller for UI
├── StageInitializer.java  # Initializes the JavaFX stage
└── UnoApplication.java    # Main application class
```

### Component Interactions

1. **UnoApplication**: Entry point that launches JavaFXApplication
2. **JavaFXApplication**: Sets up Spring context and publishes a StageReadyEvent
3. **StageInitializer**: Initializes the JavaFX stage upon receiving the StageReadyEvent
4. **MainViewController**: Controls UI interactions and updates
5. **GameService**: Communicates with the backend API

## Core Components

### UnoApplication

The main class that launches the JavaFX application. It uses `Application.launch()` to start the JavaFX application.

### JavaFXApplication

Extends `javafx.application.Application` and initializes the Spring application context. It then publishes a `StageReadyEvent` with the primary stage.

### StageInitializer

Listens for the `StageReadyEvent` and sets up the primary stage with the main-view.fxml loaded. It configures window dimensions, title, and other settings.

### MainViewController

The primary controller that:
- Initializes the UI components 
- Handles user interactions (card clicks, button clicks)
- Updates the UI based on game state
- Manages card displays and player information

Key methods:
- `initialize()`: Sets up UI components and fetches initial game state
- `displayState(GameStateDTO)`: Updates UI with game state data
- `initializeColorPicker()`: Sets up color selection for wild cards
- `displayHorizontalPlayerCards()` / `displayVerticalPlayerCards()`: Places cards in player areas
- `createCardImageView()`: Creates a card visual element with click handlers

### Data Models

- **CardDTO**: Represents a single card with properties:
  - `type`: The card type (NUMBER, SKIP, REVERSE, etc.)
  - `color`: The card color (RED, BLUE, GREEN, YELLOW, or null for wilds)
  - `number`: The card number (for number cards) or null for action cards

- **PlayerDTO**: Represents a player with:
  - `username`: The player's name
  - `deck`: List of cards in the player's hand

- **GameStateDTO**: Represents the complete game state:
  - `players`: List of players in the game
  - `currentPlayerNo`: Index of the current player's turn
  - `lastPlayedCard`: The last card played
  - `currentColor`: The current active color
  - `direction`: The direction of play (CLOCKWISE/COUNTER_CLOCKWISE)
  - `drawDeckSize`: Number of cards remaining in the draw pile
  - `gameFinished`: Whether the game has ended
  - `winner`: Username of the winner (if game is finished)

## UI Implementation

### Main View Layout

The UI is structured using a BorderPane with players positioned around a central game area:
- Player 1 (human player): Bottom
- Player 2: Left
- Player 3: Top
- Player 4: Right

The central area contains:
- Last played card
- Draw deck
- Current color indicator
- Direction indicator
- Color picker (shown only when needed)

### Card Rendering

Cards are rendered using ImageView components with images loaded from resources. The application dynamically calculates card spacing based on the number of cards in a player's hand to ensure proper layout.

For vertical layouts (players 2 and 4), cards are stacked with a small overlap to save space. For horizontal layouts (players 1 and 3), cards are arranged side by side with minimal overlap.

### Card Interactions

Cards in the human player's hand (Player 1) have click handlers that:
1. Check if it's the player's turn
2. Validate if the card can be played
3. If it's a wild card, show the color picker
4. Send the play request to the backend via GameService

## Network Communication

### REST API Endpoints

The GameService communicates with the following backend endpoints:

- `POST /api/game/start`: Starts a new game
  - Parameters: username, computerPlayerAmount
  - Returns: GameStateDTO

- `POST /api/game/play-card-human`: Plays a card from human player's hand
  - Parameters: playerId, cardIndex, cardColor (optional for wild cards)
  - Returns: GameStateDTO

- `POST /api/game/play-card-computer`: Triggers a computer player to play a card
  - Parameters: playerId
  - Returns: GameStateDTO

- `POST /api/game/draw-card`: Draws a card from the deck
  - Parameters: playerId
  - Returns: GameStateDTO

- `POST /api/game/cheat-card`: Uses a cheat card
  - Parameters: playerId, cheatType
  - Returns: GameStateDTO

### Error Handling

The application handles network errors by:
1. Catching RestClientExceptions
2. Displaying error alerts to the user
3. Logging errors to the console

## Adding New Features

### New Card Types

To add a new card type:

1. Add the new type to the backend
2. Update the CardDTO class if needed
3. Add appropriate handling in the MainViewController
4. Create card images and add them to resources

### UI Customization

To modify the UI:

1. Edit main-view.fxml using Scene Builder or text editor
2. Update corresponding methods in MainViewController
3. Add/modify CSS styles in the FXML or through code

### New Game Rules

To implement new game rules:

1. Modify the backend to implement the rule logic
2. Update the GameService to handle any new API endpoints
3. Enhance the UI to reflect the new rules or actions

## Common Issues & Solutions

### Card Image Loading

If card images aren't displaying properly:
- Ensure all images are in the resources/cards directory
- Verify the naming convention matches what's expected in code
- Check the file paths used in MainViewController.createCardImageView()

### UI Sizing Issues

If UI components aren't sized correctly:
- Adjust minWidth and minHeight values in main-view.fxml
- Modify the dynamic spacing calculations in calculateDynamicHorizontalSpacing() and calculateDynamicVerticalSpacing()
- Ensure card images are of consistent dimensions

### Network Communication

If communication with the backend fails:
- Verify the api.base-url in application.properties is correct
- Check that the backend server is running
- Review parameters being sent in API requests

## Performance Considerations

- The application uses lazy loading for card images to improve startup time
- UI updates are performed on the JavaFX Application Thread to prevent concurrency issues
- Network requests are made asynchronously where possible to prevent UI freezing

## Code Style Guidelines

- Follow Java naming conventions
- Use dependency injection through Spring's DI system
- Keep UI logic in controllers and business logic in services
- Document public methods with Javadoc comments
- Use appropriate visibility modifiers (private, protected, public)

## Testing Strategies

### Unit Testing

Create unit tests for:
- Service classes with mocked HTTP responses
- Model classes for proper serialization/deserialization
- Utility methods and calculations

### Integration Testing

Test the integration between:
- UI components and controllers
- Services and the actual backend (using test server)

### UI Testing

Use TestFX framework to test:
- Card interactions
- Button clicks
- Game flow scenarios 