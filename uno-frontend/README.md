# UNO Game Frontend

A JavaFX-based frontend application for the UNO card game developed using Spring Boot and JavaFX.

## Project Overview

This frontend application provides a graphical user interface for playing the classic UNO card game. It communicates with a backend server that handles the game logic and state. The frontend is responsible for displaying the game state, handling user interactions, and providing a visually appealing interface for players.

## Tech Stack

- **Java**: Core programming language
- **JavaFX**: GUI framework for desktop applications
- **Spring Boot**: Application framework for dependency injection and configuration
- **Spring RestTemplate**: For REST API communication with the backend
- **Maven**: Dependency management and build tool
- **Lombok**: Reduces boilerplate code for model classes

## Architecture

### Main Components

- **UnoApplication.java**: Entry point of the application
- **JavaFXApplication.java**: JavaFX application class that sets up the Spring context
- **MainViewController.java**: Main controller that handles UI interactions and updates
- **GameService.java**: Service class that communicates with the backend REST API
- **Model classes**: Data transfer objects for game state and card information

### Package Structure

- **tr.edu.metu.ceng.uno**: Root package
  - **.config**: Configuration classes like RestTemplate setup
  - **.model**: Data models (DTOs) for communication with backend
  - **.service**: Services for handling business logic and API calls
  - **.view**: Custom UI components

## Features

1. **Game Visualization**:
   - Display of up to 4 players around a virtual table
   - Card visualization for each player's hand
   - Central game area showing the last played card, current color, and direction

2. **Game Mechanics**:
   - Playing cards from hand
   - Drawing cards from the deck
   - Handling special cards (Reverse, Skip, Draw Two, Wild, Wild Draw Four)
   - Color selection for Wild cards

3. **Cheat Features**:
   - Ability to use special action cards as cheats

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- Maven 3.6 or higher

### Running the Application

1. Clone the repository:
```
git clone https://github.com/your-username/CENG453_20242_Group1_frontend.git
```

2. Navigate to the project directory:
```
cd CENG453_20242_Group1_frontend
```

3. Build the project:
```
mvn clean install
```

4. Run the application:
```
mvn spring-boot:run
```

### Configuration

The application can be configured through the `src/main/resources/application.properties` file:

- `spring.application.name`: Name of the application
- `spring.application.ui.title`: Window title
- `api.base-url`: URL of the backend API server (default: http://localhost:8080)

## Game Flow

1. When the application starts, it automatically connects to the backend server and starts a new game
2. The human player (Player 1) is positioned at the bottom of the screen
3. The current player is highlighted with a different style
4. Players can play cards by clicking on them from their hand
5. If a Wild card is played, a color picker will appear to select the new color
6. Computer players automatically play their turns

## API Communication

The frontend communicates with the backend through the following REST endpoints:

- **POST /api/game/start**: Starts a new game
- **POST /api/game/play-card-human**: Plays a card from the human player's hand
- **POST /api/game/play-card-computer**: Triggers a computer player to play a card
- **POST /api/game/draw-card**: Draws a card from the deck
- **POST /api/game/cheat-card**: Uses a cheat card

## UI Components

### Main Layout

The UI is structured as a BorderPane with five main areas:
- **Top**: Player 3's area (opponent)
- **Left**: Player 2's area (opponent)
- **Right**: Player 4's area (opponent)
- **Center**: Game information area (last played card, draw pile, current color, direction)
- **Bottom**: Player 1's area (human player)

### Card Display

- Cards are displayed as ImageView components with images loaded from resources
- Custom CardImageView class extends ImageView to store additional player information
- Horizontal layout for players at the top and bottom
- Vertical layout for players on the left and right

## Development

### Adding Features

1. To add new game mechanics, modify the GameService to add appropriate API calls
2. To enhance UI, update the main-view.fxml file and corresponding controller methods
3. Add new card types by extending the model and service layers

### Debugging

The application uses SLF4J with Logback for comprehensive logging. Log levels (DEBUG, INFO, WARN, ERROR) are used appropriately throughout the codebase. The logging configuration can be found in `src/main/resources/logback-spring.xml`.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is part of the CENG453 course at Middle East Technical University and is subject to university policies.

## Acknowledgments

- Developed as part of CENG453 course at METU
- Based on the classic UNO card game by Mattel
