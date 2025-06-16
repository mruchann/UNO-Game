# UNO Backend Database Schema

This document outlines the database schema for the UNO backend application.

## Entity Relationship Diagram

```
+----------------+       +----------------+       +----------------+
|      USER      |       |  GAME_RECORD   |       | LEADERBOARD_   |
|                |       |                |       |    RECORD      |
+----------------+       +----------------+       +----------------+
| id (PK)        |<----->| id (PK)        |<----->| id (PK)        |
| username       |       | date           |       | user_id (FK)   |
| email          |       | duration       |       | score          |     
| password       |       | status         |       | games_played   |     
+----------------+       +----------------+       | games_won      |     
                                                  +----------------+     
```

## Tables

### USER

Stores user account information.

| Column     | Type         | Constraints     | Description                      |
|------------|--------------|-----------------|----------------------------------|
| id         | UUID         | PK              | Unique identifier                |
| username   | VARCHAR(50)  | UNIQUE, NOT NULL| User's login name                |
| email      | VARCHAR(100) | UNIQUE, NOT NULL| User's email address             |
| password   | VARCHAR(100) | NOT NULL        | Hashed password (BCrypt)         |

### GAME_RECORD

Stores information about completed games.

| Column     | Type         | Constraints     | Description                      |
|------------|--------------|-----------------|----------------------------------|
| id         | UUID         | PK              | Unique identifier                |
| date       | TIMESTAMP    | NOT NULL        | When the game was played         |
| duration   | INT          | NOT NULL        | Game duration in seconds         |
| status     | VARCHAR(20)  | NOT NULL        | Game status (COMPLETED, etc.)    |

### PLAYER_RECORD

Records player participation and performance in games.

| Column      | Type        | Constraints         | Description                      |
|-------------|-------------|---------------------|----------------------------------|
| id          | UUID        | PK                  | Unique identifier                |
| game_id     | UUID        | FK (GAME_RECORD.id) | Reference to the game            |
| user_id     | UUID        | FK (USER.id), NULL  | Reference to user (NULL for computer) |
| score       | INT         | NOT NULL            | Player's score in the game       |
| result      | VARCHAR(10) | NOT NULL            | WIN or LOSS                      |
| is_computer | BOOLEAN     | NOT NULL            | Whether this is a computer player|

### LEADERBOARD_RECORD

Tracks player rankings and statistics.

| Column        | Type      | Constraints     | Description                      |
|---------------|-----------|-----------------|----------------------------------|
| id            | UUID      | PK              | Unique identifier                |
| user_id       | UUID      | FK (USER.id)    | Reference to the user            |
| score         | INT       | NOT NULL        | Total accumulated score          |
| games_played  | INT       | NOT NULL        | Number of games played           |
| games_won     | INT       | NOT NULL        | Number of games won              |

## Indexes

| Table              | Index Name            | Columns                    | Type    |
|--------------------|----------------------|----------------------------|---------|
| USER               | user_username_idx    | username                   | UNIQUE  |
| USER               | user_email_idx       | email                      | UNIQUE  |
| GAME_RECORD        | game_date_idx        | date                       | BTREE   |
| PLAYER_RECORD      | player_game_idx      | game_id                    | BTREE   |
| PLAYER_RECORD      | player_user_idx      | user_id                    | BTREE   |
| LEADERBOARD_RECORD | leaderboard_user_idx | user_id                    | UNIQUE  |
| LEADERBOARD_RECORD | leaderboard_score_idx| score                      | BTREE   |

## Relationships

1. **User to PlayerRecord**: One-to-Many
   - A user can participate in multiple games
   - Each player record (except computer players) belongs to one user

2. **GameRecord to PlayerRecord**: One-to-Many
   - A game has multiple players (2-4)
   - Each player record belongs to exactly one game

3. **User to LeaderboardRecord**: One-to-One
   - Each user has one leaderboard record
   - Each leaderboard record belongs to one user

## Data Types

- **UUID**: 128-bit universally unique identifier
- **VARCHAR**: Variable-length character string
- **TIMESTAMP**: Date and time
- **INT**: 32-bit integer
- **BOOLEAN**: True/false value

## Schema Creation

The tables are automatically created by Hibernate using the `spring.jpa.hibernate.ddl-auto=update` configuration.

## Sample SQL Schema

```sql
-- USER Table
CREATE TABLE user (
    id CHAR(36) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY user_username_idx (username),
    UNIQUE KEY user_email_idx (email)
);

-- GAME_RECORD Table
CREATE TABLE game_record (
    id CHAR(36) NOT NULL,
    date TIMESTAMP NOT NULL,
    duration INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    KEY game_date_idx (date)
);

-- PLAYER_RECORD Table
CREATE TABLE player_record (
    id CHAR(36) NOT NULL,
    game_id CHAR(36) NOT NULL,
    user_id CHAR(36),
    score INT NOT NULL,
    result VARCHAR(10) NOT NULL,
    is_computer BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    KEY player_game_idx (game_id),
    KEY player_user_idx (user_id),
    CONSTRAINT fk_player_game FOREIGN KEY (game_id) REFERENCES game_record (id),
    CONSTRAINT fk_player_user FOREIGN KEY (user_id) REFERENCES user (id)
);

-- LEADERBOARD_RECORD Table
CREATE TABLE leaderboard_record (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    games_played INT NOT NULL DEFAULT 0,
    games_won INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY leaderboard_user_idx (user_id),
    KEY leaderboard_score_idx (score),
    CONSTRAINT fk_leaderboard_user FOREIGN KEY (user_id) REFERENCES user (id)
);
``` 