# UNO Backend Documentation

## Project Overview
This is a backend implementation of the classic UNO card game, built as a multiplayer online game using Spring Boot. The project provides RESTful APIs for user management, game mechanics, leaderboard tracking, and game history.

## Technologies Used
- **Java 21**: Core programming language
- **Spring Boot 3.3.9**: Main application framework
- **Spring Data JPA**: Data persistence
- **Spring Security**: Authentication and authorization
- **Spring Mail**: Email services for user verification
- **MariaDB**: Database
- **Swagger/OpenAPI**: API documentation
- **JWT**: JSON Web Token for stateless authentication
- **Maven**: Build and dependency management
- **Lombok**: Reduces boilerplate code
- **Hibernate Validator**: Data validation

## System Architecture
The application follows a standard Spring Boot architecture with the following layers:
- **Entity Layer**: Domain models (User, Game, Card, etc.)
- **Repository Layer**: Database access interfaces
- **Service Layer**: Business logic implementation
- **Controller Layer**: RESTful API endpoints
- **DTO Layer**: Data transfer objects for API requests/responses
- **Config Layer**: Application configuration

## API Documentation
The API documentation is available via Swagger UI at:
- https://ceng453-20242-group1-backend.onrender.com/api

## Key Features

### User Management
- User registration and authentication
- Email verification
- Password reset functionality
- JWT-based authentication

### Game Mechanics
- Implementation of UNO game rules
- Support for 2-4 players
- Computer players implementation
- Turn-based gameplay
- Card actions (Skip, Reverse, Draw Two, Wild, etc.)

### Card System
- Different card types: NumberCard, ActionCard, WildCard
- Card colors and values based on official UNO rules
- Card actions and effects

### Player System
- Support for human and computer players
- Player deck management
- Player actions (draw card, play card, call "UNO")

### Game History
- Tracking of game results
- Historical game data retrieval
- Player statistics

### Leaderboard
- Player rankings based on game performance
- Score tracking

## Core Components

### User Component
- User registration and authentication
- Profile management

### Game Component
- Game creation and management
- Game state handling
- Turn processing
- Game rules implementation

### Card Component
- Card types and properties
- Card deck generation and management

### Player Component
- Player actions and state
- Player deck management
- Human and computer player implementations

### History Component
- Game records storage
- Statistics calculation

### Leaderboard Component
- Player rankings
- Score tracking

## Database Schema
The application uses MariaDB with JPA/Hibernate for ORM. For a detailed overview of the database structure, including tables, relationships, indexes, and sample SQL schema, please refer to the [Database Schema](Database_Schema.md) document.

Key entities include:
- **User**: Stores user credentials and information
- **GameRecord**: Stores game history and results
- **PlayerRecord**: Stores player participation and performance
- **LeaderboardRecord**: Stores player rankings and scores

## Security
- BCrypt password encryption
- CSRF protection disabled (for API access)
- Stateless authentication with JWT

## Email Service
- SMTP configuration for Gmail
- Email templates for verification and notifications

## Deployment
The application is deployed on Render at:
- https://ceng453-20242-group1-backend.onrender.com/

## Development Setup

### Prerequisites
- Java 21
- Maven
- MariaDB

### Building the Project
```bash
mvn clean install
```

### Running the Project
```bash
mvn spring-boot:run
```

### Docker
A Dockerfile is provided for containerization.

## Testing
The project includes JUnit tests for various components.

## Postman Collection
A Postman collection is available for API testing at:
- https://github.com/mruchann/CENG453_20242_Group1_backend/tree/master/src/main/java/tr/edu/metu/ceng/uno/postman 