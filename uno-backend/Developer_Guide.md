# UNO Backend Developer Guide

This guide provides information for developers working on the UNO backend project.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21
- Maven 3.6+
- Git
- Your favorite IDE (IntelliJ IDEA recommended)
- MariaDB (for local development)

### Setting Up the Development Environment

1. Clone the repository:
   ```bash
   git clone https://github.com/mruchann/CENG453_20242_Group1_backend.git
   cd CENG453_20242_Group1_backend
   ```

2. Set up the database:
   - Create a local MariaDB database
   - Configure application.properties with your database credentials

3. Import the project into your IDE:
   - For IntelliJ IDEA, use "Import Project" and select the pom.xml file
   - For Eclipse, use "Import Maven Project"

4. Build the project:
   ```bash
   mvn clean install
   ```

5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Project Structure

The project follows a standard Spring Boot application structure with clear separation of concerns:

```
src/
├── main/
│   ├── java/
│   │   └── tr/
│   │       └── edu/
│   │           └── metu/
│   │               └── ceng/
│   │                   └── uno/
│   │                       ├── card/            # Card-related classes
│   │                       ├── common/          # Common utilities and helpers
│   │                       ├── config/          # Configuration classes
│   │                       ├── dto/             # Data Transfer Objects
│   │                       ├── email/           # Email service
│   │                       ├── exception/       # Custom exceptions
│   │                       ├── game/            # Game mechanics
│   │                       ├── history/         # Game history tracking
│   │                       ├── leaderboard/     # Leaderboard functionality
│   │                       ├── player/          # Player-related classes
│   │                       ├── postman/         # Postman collection for API testing
│   │                       ├── template/        # Email templates
│   │                       ├── user/            # User management
│   │                       ├── util/            # Utility classes
│   │                       └── UnoApplication.java  # Main application class
│   └── resources/
│       ├── static/         # Static resources
│       ├── templates/      # Thymeleaf templates
│       └── application.properties  # Application configuration
└── test/
    └── java/
        └── tr/
            └── edu/
                └── metu/
                    └── ceng/
                        └── uno/
                            └── # Test classes
```

## Architecture Overview

The application follows a layered architecture:

1. **Controller Layer**: Handles HTTP requests and responses
2. **Service Layer**: Contains business logic
3. **Repository Layer**: Interfaces with the database
4. **Entity Layer**: Domain models representing database tables

### Key Components

- **User Management**: Authentication, registration, profile management
- **Game Engine**: Core game mechanics
- **Card System**: Card types and behaviors
- **Player System**: Player actions and state
- **History Tracking**: Game records and statistics
- **Leaderboard**: Player rankings

## Coding Standards

### Java Code Style

- Follow standard Java naming conventions:
  - Classes: PascalCase (e.g., `GameController`)
  - Methods and variables: camelCase (e.g., `getPlayerScore()`)
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_PLAYERS`)
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Include Javadoc comments for all public methods and classes

### Package Structure

- Follow the established package structure
- Place related classes in the same package
- Use sub-packages for better organization

### Design Patterns

- Use the Repository pattern for data access
- Follow the Service pattern for business logic
- Implement DTO pattern for API request/response objects
- Use Dependency Injection for loose coupling

## Working with the Game Engine

The game engine is the core of the application. It handles the game state, rules, and player interactions.

### Game Flow

1. **Game Initialization**:
   ```java
   List<Player> players = new ArrayList<>();
   players.add(new HumanPlayer("player1"));
   players.add(new ComputerPlayer("computer1"));
   Game game = new Game(players);
   game.distributeCards();
   game.initializeFirstCard();
   ```

2. **Playing a Turn**:
   ```java
   // Check if valid move
   boolean isValidMove = CardUtil.isValidMove(card, game.getLastPlayedCard(), game.getCurrentCardColor());
   
   // Process the move
   game.setLastPlayedCard(card);
   game.setNextColor();
   
   // Handle special cards
   if (card.getCardType() == CardType.SKIP) {
       game.setNextPlayerUnconditionally();
   } else if (card.getCardType() == CardType.REVERSE) {
       game.setDirection(game.getDirection().reverse());
   }
   
   // Move to next player
   game.setNextPlayerConditionally();
   ```

3. **Drawing Cards**:
   ```java
   // Draw a card
   Card drawnCard = game.getDrawDeck().removeFirst();
   player.drawCard(drawnCard);
   
   // Refill draw deck if empty
   if (game.getDrawDeck().isEmpty()) {
       game.refillDrawDeck();
   }
   ```

### Adding New Card Types

To add a new card type:

1. Add the new type to the `CardType` enum
2. Create a new class extending the appropriate base class (e.g., `ActionCard`)
3. Implement the required behavior in the `Game` class

## Testing

### Unit Testing

The project uses JUnit for unit testing. Write tests for all service methods and utility classes.

Example test:
```java
@Test
public void testIsValidMove() {
    Card lastPlayed = new NumberCard(CardColor.RED, 5);
    Card current = new NumberCard(CardColor.RED, 7);
    
    assertTrue(CardUtil.isValidMove(current, lastPlayed, CardColor.RED));
    
    current = new NumberCard(CardColor.BLUE, 5);
    assertTrue(CardUtil.isValidMove(current, lastPlayed, CardColor.RED));
    
    current = new NumberCard(CardColor.BLUE, 8);
    assertFalse(CardUtil.isValidMove(current, lastPlayed, CardColor.RED));
}
```

### Integration Testing

Write integration tests to verify that different components work together correctly:

```java
@SpringBootTest
public class GameServiceIntegrationTest {
    @Autowired
    private SingleplayerGameService singleplayerGameService;
    
    @Autowired
    private UserService userService;
    
    @Test
    public void testCreateAndStartGame() {
        // Test game creation and initialization
    }
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=SingleplayerGameServiceTest

# Run a specific test method
mvn test -Dtest=SingleplayerGameServiceTest#testCreateGame
```

## API Documentation

The project uses Swagger/OpenAPI for API documentation. Access the API documentation at:

```
http://localhost:8080/api
```

### Adding New Endpoints

1. Create a new controller class or add methods to an existing controller
2. Annotate the controller with `@RestController` and `@RequestMapping`
3. Annotate methods with appropriate HTTP method annotations (`@GetMapping`, `@PostMapping`, etc.)
4. Add Swagger annotations for better documentation

Example:
```java
@RestController
@RequestMapping("/games")
public class GameController {
    
    @PostMapping("/{gameId}/play")
    public GameState playCard(@PathVariable UUID gameId, @RequestBody PlayCardRequest request) {
        // Implementation
    }
}
```

## Debugging

### Logging

The project uses Spring Boot's default logging (Logback). Add logging statements for important events:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(SingleplayerGameService.class);

public void processPlayerTurn() {
    logger.debug("Processing turn for player: {}", game.getCurrentPlayer());
    // Implementation
    logger.info("Turn processed successfully");
}
```

### Common Issues

1. **Database Connection Issues**:
   - Check database credentials in application.properties
   - Verify that the database server is running

2. **JWT Authentication Issues**:
   - Verify that the token is being sent correctly
   - Check token expiration

## Contributing

### Git Workflow

1. Create a new branch for each feature or bug fix:
   ```bash
   git checkout -b feature/new-feature
   ```

2. Make changes and commit with descriptive messages:
   ```bash
   git commit -m "Add new feature: detailed description"
   ```

3. Push your branch to the remote repository:
   ```bash
   git push origin feature/new-feature
   ```

4. Create a pull request on GitHub

### Code Reviews

All code changes should be reviewed by at least one other developer. Follow these guidelines for code reviews:

1. Review for functionality: Does the code work as expected?
2. Review for code quality: Is the code clean, maintainable, and following standards?
3. Review for security: Are there any security vulnerabilities?
4. Review for performance: Are there any performance concerns?

### Continuous Integration

The project uses GitHub Actions for CI/CD. The pipeline includes:

1. Building the project
2. Running tests
3. Analyzing code quality
4. Deploying to staging (for specific branches)

## Performance Considerations

### Database Optimization

- Use indexes for frequently queried columns
- Use pagination for large result sets
- Write efficient queries

### Memory Management

- Avoid creating unnecessary objects
- Use streams for large collections
- Consider caching frequently accessed data

## Security

### Authentication

The project uses JWT for authentication. The token is generated upon login and should be included in the `Authorization` header for protected endpoints.

### Input Validation

Always validate user input:
- Use Bean Validation (JSR 380) with annotations like `@NotNull`, `@Size`, etc.
- Sanitize input to prevent SQL injection and XSS attacks

### Password Storage

Passwords are stored using BCrypt hashing:
```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
``` 