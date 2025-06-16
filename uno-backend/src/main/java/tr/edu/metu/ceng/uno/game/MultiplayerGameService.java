package tr.edu.metu.ceng.uno.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tr.edu.metu.ceng.uno.card.Card;
import tr.edu.metu.ceng.uno.card.CardColor;
import tr.edu.metu.ceng.uno.card.CardType;
import tr.edu.metu.ceng.uno.card.NumberCard;
import tr.edu.metu.ceng.uno.dto.GameStateDTO;
import tr.edu.metu.ceng.uno.history.GameHistoryService;
import tr.edu.metu.ceng.uno.history.request.CreateGameRecordRequest;
import tr.edu.metu.ceng.uno.player.HumanPlayer;
import tr.edu.metu.ceng.uno.player.Player;
import tr.edu.metu.ceng.uno.util.CardUtil;
import tr.edu.metu.ceng.uno.websocket.MultiplayerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MultiplayerGameService {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameHistoryService gameHistoryService;

    // Queue of players waiting for multiplayer games
    private final List<String> waitingPlayers = new ArrayList<>();

    // Map of active multiplayer games: gameId -> Game
    private final Map<String, Game> multiplayerGames = new ConcurrentHashMap<>();

    // Map of players to game IDs: username -> gameId
    private final Map<String, String> playerGameMap = new ConcurrentHashMap<>();

    public MultiplayerGameService(SimpMessagingTemplate messagingTemplate, GameHistoryService gameHistoryService) {
        this.messagingTemplate = messagingTemplate;
        this.gameHistoryService = gameHistoryService;
        log.info("MultiplayerGameService initialized");
        log.debug("Active games: {}, Waiting players: {}", multiplayerGames.size(), waitingPlayers.size());
    }

    /**
     * Process joining a multiplayer game room
     * @param username The username of the player joining
     * @return The gameId if game started, null if waiting
     */
    public String joinRoom(String username) {
        log.info("Player {} attempting to join multiplayer room", username);

        synchronized (waitingPlayers) {
            // Check if player is already in a game
            if (playerGameMap.containsKey(username)) {
                String existingGameId = playerGameMap.get(username);
                log.info("Player {} is already in game {}", username, existingGameId);
                return existingGameId;
            }

            // Check if player is already waiting
            if (waitingPlayers.contains(username)) {
                log.info("Player {} is already in waiting room", username);
                return null; // Already waiting
            }

            // Add player to waiting list
            waitingPlayers.add(username);
            log.info("Player {} added to waiting room. Total waiting: {}", username, waitingPlayers.size());

            // Send waiting message
            messagingTemplate.convertAndSend("/topic/lobby", 
                MultiplayerMessage.builder()
                    .type(MultiplayerMessage.MessageType.ROOM_JOIN)
                    .username(username)
                    .content("Player " + username + " joined the waiting room")
                    .build());
            log.debug("Sent room join message to lobby for player {}", username);

            // If we have 2 players waiting, start a new game
            if (waitingPlayers.size() >= 2) {
                String player1 = waitingPlayers.remove(0);
                String player2 = waitingPlayers.remove(0);

                log.info("Starting new game with players {} and {}", player1, player2);
                return createMultiplayerGame(player1, player2);
            }

            log.info("Player {} is now waiting for another player", username);
            return null; // Still waiting for another player
        }
    }

    /**
     * Creates a new multiplayer game with two human players
     * @param player1 First player's username
     * @param player2 Second player's username
     * @return The game ID
     */
    private String createMultiplayerGame(String player1, String player2) {
        log.info("Creating new multiplayer game for players {} and {}", player1, player2);

        String gameId = UUID.randomUUID().toString();
        log.debug("Generated game ID: {}", gameId);

        List<Player> players = new ArrayList<>();
        players.add(new HumanPlayer(player1));
        players.add(new HumanPlayer(player2));

        log.debug("Creating game instance with {} players", players.size());
        Game game = new Game(players);

        log.debug("Distributing initial cards to players");
        game.distributeCards();

        log.debug("Initializing first card");
        game.initializeFirstCard();

        // Store the game in our map
        multiplayerGames.put(gameId, game);
        log.debug("Game added to active games map. Total active games: {}", multiplayerGames.size());

        // Map players to this game
        playerGameMap.put(player1, gameId);
        playerGameMap.put(player2, gameId);
        log.debug("Players mapped to game ID in player-game map");

        // Notify both players that the game has started
        GameStateDTO gameState = GameStateDTO.createGameStateDTO(game);
        log.debug("Game state DTO created. Current player: {}", gameState.getCurrentPlayerNo());

        // Create the game start message
        MultiplayerMessage gameStartMessage = MultiplayerMessage.builder()
            .type(MultiplayerMessage.MessageType.GAME_START)
            .gameId(gameId)
            .content("Game started with players: " + player1 + " and " + player2)
            .build();

        // Send to both the lobby topic (where waiting players are listening)
        // and the game-specific topic for future messages
        log.debug("Sending game start message to lobby and game-specific topics");
        messagingTemplate.convertAndSend("/topic/lobby", gameStartMessage);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, gameStartMessage);

        log.info("Multiplayer game {} created successfully for players {} and {}", gameId, player1, player2);
        return gameId;
    }

    /**
     * Get the current game state for a specific game
     * @param gameId The game ID
     * @return GameStateDTO containing the current game state
     */
    public GameStateDTO getGameState(String gameId) {
        log.debug("Getting game state for game ID: {}", gameId);

        Game game = multiplayerGames.get(gameId);
        if (game == null) {
            log.error("No game found with ID: {}", gameId);
            throw new IllegalStateException("No game found with ID: " + gameId);
        }

        GameStateDTO gameState = GameStateDTO.createGameStateDTO(game);
        log.debug("Game state retrieved for game {}. Current player: {}, Players count: {}", 
                gameId, gameState.getCurrentPlayerNo(), gameState.getPlayers().size());

        return gameState;
    }

    /**
     * Process drawing a card in multiplayer mode
     * @param gameId The game ID
     * @param username The username of the player drawing a card
     * @param playerIndex The index of the player drawing a card
     * @return Updated game state
     */
    public GameStateDTO drawCard(String gameId, String username, int playerIndex) {
        log.info("Player {} (index: {}) attempting to draw a card in game {}", username, playerIndex, gameId);

        // Get the game
        Game game = multiplayerGames.get(gameId);
        if (game == null) {
            log.error("No game found with ID: {}", gameId);
            throw new IllegalStateException("No game found with ID: " + gameId);
        }
        log.debug("Game {} found. Current player index: {}", gameId, game.getCurrentPlayer());

        // Validate it's the player's turn
        if (playerIndex != game.getCurrentPlayer()) {
            log.warn("Player {} (index: {}) attempted to draw a card out of turn. Current player index: {}", 
                    username, playerIndex, game.getCurrentPlayer());
            throw new IllegalArgumentException("Not your turn to draw a card");
        }
        log.debug("Validated it's player {}'s turn", username);

        // Validate the player is who they claim to be
        Player player = game.getPlayers().get(playerIndex);
        if (!player.getUsername().equals(username)) {
            log.warn("Player username mismatch. Expected: {}, Actual: {}", player.getUsername(), username);
            throw new IllegalArgumentException("Player username does not match");
        }
        log.debug("Validated player identity: {}", username);

        // Check if player should draw a card
        log.debug("Checking if player {} should draw a card", username);
        log.trace("Last played card: {} {}, Current color: {}", 
                game.getLastPlayedCard().getCardColor(), 
                game.getLastPlayedCard().getCardType(), 
                game.getCurrentCardColor());

        if (player.shouldDrawCard(game.getLastPlayedCard(), game.getCurrentCardColor())) {
            log.info("Player {} is drawing a card", username);
            log.debug("Draw deck size before drawing: {}", game.getDrawDeck().size());

            Card cardToDraw = game.getDrawDeck().removeFirst();
            log.debug("Card drawn: {} {}", cardToDraw.getCardColor(), cardToDraw.getCardType());

            player.drawCard(cardToDraw);
            log.debug("Card added to player {}'s deck. New deck size: {}", 
                    username, player.getDeck().size());

            // Check if draw deck needs refilling
            log.debug("Checking if draw deck needs refilling");
            game.refillDrawDeck();

            // Check if the drawn card can be played
            boolean canPlayDrawnCard = CardUtil.isValidMove(
                    cardToDraw, game.getLastPlayedCard(), game.getCurrentCardColor());
            log.debug("Can player {} play the drawn card? {}", username, canPlayDrawnCard);

            if (!canPlayDrawnCard) {
                log.info("Player {} cannot play the drawn card. Moving to next player", username);
                game.setNextPlayerUnconditionally();
            } else {
                log.info("Player {} can play the drawn card if they choose to", username);
            }
        } else {
            log.info("Player {} has valid cards to play and should not draw", username);
        }

        // Notify all players of the state change
        log.debug("Sending draw card notification to all players in game {}", gameId);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, 
            MultiplayerMessage.builder()
                .type(MultiplayerMessage.MessageType.DRAW_CARD)
                .gameId(gameId)
                .username(username)
                .playerIndex(playerIndex)
                .build());
        log.debug("Draw card notification sent");

        log.debug("Getting updated game state");
        GameStateDTO updatedState = getGameState(gameId);
        log.debug("Returning updated game state. Current player: {}", updatedState.getCurrentPlayerNo());

        return updatedState;
    }

    /**
     * Process playing a card in multiplayer mode
     * @param gameId The game ID
     * @param username The username of the player playing a card
     * @param playerIndex The index of the player playing a card
     * @param cardIndex The index of the card being played
     * @param cardColor Selected color for wild cards
     * @return Updated game state
     */
    public GameStateDTO playCard(String gameId, String username, int playerIndex, int cardIndex, CardColor cardColor) {
        log.info("Player {} (index: {}) attempting to play card at index {} with color {} in game {}", 
                username, playerIndex, cardIndex, cardColor, gameId);

        // Get the game
        Game game = multiplayerGames.get(gameId);
        if(game.isCanChallenge()){
            log.warn("Player {} (index: {}) attempted to play a card in a game in which challenge is active. ", username, playerIndex);
            return getGameState(gameId);
        }
        if (game == null) {
            log.error("No game found with ID: {}", gameId);
            throw new IllegalStateException("No game found with ID: " + gameId);
        }
        log.debug("Game {} found. Current player index: {}", gameId, game.getCurrentPlayer());

        // Validate it's the player's turn
        if (playerIndex != game.getCurrentPlayer()) {
            log.warn("Player {} (index: {}) attempted to play a card out of turn. Current player index: {}", 
                    username, playerIndex, game.getCurrentPlayer());
            throw new IllegalArgumentException("Not your turn to play a card");
        }
        log.debug("Validated it's player {}'s turn", username);

        // Validate the player is who they claim to be
        Player player = game.getPlayers().get(playerIndex);
        if (!player.getUsername().equals(username)) {
            log.warn("Player username mismatch. Expected: {}, Actual: {}", player.getUsername(), username);
            throw new IllegalArgumentException("Player username does not match");
        }
        log.debug("Validated player identity: {}", username);

        // Validate card index
        if (cardIndex < 0 || cardIndex >= player.getDeck().size()) {
            log.warn("Invalid card index: {}. Player {} has {} cards", 
                    cardIndex, username, player.getDeck().size());
            throw new IllegalArgumentException("Invalid card index");
        }
        log.debug("Validated card index: {}", cardIndex);

        Card cardToPlay = player.getDeck().get(cardIndex);
        log.debug("Card to play: {} {}", cardToPlay.getCardColor(), cardToPlay.getCardType());

        // Log current game state before play
        log.debug("Current game state - Last played card: {} {}, Current color: {}, Direction: {}", 
                game.getLastPlayedCard().getCardColor(), 
                game.getLastPlayedCard().getCardType(), 
                game.getCurrentCardColor(),
                game.getDirection());

        // Validate the card can be played
        if (!CardUtil.isValidMove(cardToPlay, game.getLastPlayedCard(), game.getCurrentCardColor())) {
            log.warn("Invalid move attempted by player {}. Card {} {} cannot be played on {} {} with current color {}", 
                    username, 
                    cardToPlay.getCardColor(), cardToPlay.getCardType(),
                    game.getLastPlayedCard().getCardColor(), game.getLastPlayedCard().getCardType(),
                    game.getCurrentCardColor());
            throw new IllegalArgumentException("Invalid move");
        }
        log.debug("Validated card can be played");

        // Remove the card from player's deck
        player.getDeck().remove(cardIndex);
        log.debug("Card removed from player {}'s deck. Remaining cards: {}", 
                username, player.getDeck().size());
        if(cardToPlay.getCardType() == CardType.WILD_DRAW_FOUR){
            game.setLastPlayedCardBeforeWildDrawFour(game.getLastPlayedCard());//will be used to determine challenge result
            game.getLastPlayedCardBeforeWildDrawFour().setCardColor(game.getCurrentCardColor()); //any case of wild card handling just before wild draw four
            game.setCanChallenge(true);
            System.out.println("WILD_DRAW_FOUR card played and the previous card played is: " + game.getLastPlayedCardBeforeWildDrawFour().getCardType()
                    + "/" + game.getLastPlayedCardBeforeWildDrawFour().getCardColor()  );
            if(game.getLastPlayedCardBeforeWildDrawFour().isNumberCard())
                System.out.println("And the value is:" + ((NumberCard)game.getLastPlayedCardBeforeWildDrawFour()).getCardNumber());
        }
        // Set as last played card
        game.setLastPlayedCard(cardToPlay);
        game.getDiscardDeck().add(cardToPlay);
        log.info("Player {} played card: {} {}", 
                username, cardToPlay.getCardColor(), cardToPlay.getCardType());
        log.debug("Card added to discard deck. Discard deck size: {}", game.getDiscardDeck().size());

        // Handle card effects
        if (cardToPlay.getCardType() == CardType.REVERSE) {
            Direction oldDirection = game.getDirection();
            Direction newDirection = oldDirection.reverse();
            log.info("REVERSE card played. Changing direction from {} to {}", oldDirection, newDirection);
            game.setDirection(newDirection);
        } else if (cardToPlay.getCardType() == CardType.SKIP) {
            log.info("SKIP card played. Skipping next player's turn");
            log.debug("Using setNextPlayerConditionally to handle SKIP effect");
            game.setNextPlayerConditionally(); // Move to next player with skip logic
        }

        // Handle color selection for wild cards
        if (cardToPlay.isWildCard()) {
            log.debug("Wild card played. Handling color selection");
            if (cardColor != null) {
                log.info("Setting color to player-selected color: {}", cardColor);
                game.setCurrentCardColor(cardColor);
            } else {
                log.info("No color selected for wild card. Defaulting to RED");
                game.setCurrentCardColor(CardColor.RED);
            }
        } else {
            log.debug("Setting color to card's color: {}", cardToPlay.getCardColor());
            game.setCurrentCardColor(cardToPlay.getCardColor());
        }

        // Handle draw cards
        if (cardToPlay.getCardType() == CardType.DRAW_TWO) {
            log.info("DRAW_TWO card played");
            int oldStackAmount = game.getDrawTwoStackAmount();
            int newStackAmount = oldStackAmount + 1;
            game.setDrawTwoStackAmount(newStackAmount);
            log.debug("Draw two stack amount increased from {} to {}", oldStackAmount, newStackAmount);

            // Move to next player first
            log.debug("Moving to next player to handle DRAW_TWO effect");
            game.setNextPlayerUnconditionally();

            int nextPlayerIndex = game.getCurrentPlayer();
            Player nextPlayer = game.getPlayers().get(nextPlayerIndex);
            log.debug("Next player is {} (index: {})", nextPlayer.getUsername(), nextPlayerIndex);

            log.debug("Checking if next player {} has a DRAW_TWO card", nextPlayer.getUsername());
            boolean hasDrawTwo = nextPlayer.getDeck().stream()
                    .anyMatch(card -> card.getCardType() == CardType.DRAW_TWO);
            log.debug("Next player has DRAW_TWO card: {}", hasDrawTwo);

            if (!hasDrawTwo) {
                int cardsToDraw = 2 * game.getDrawTwoStackAmount();
                log.info("Player {} must draw {} cards (2 × {} stack)", 
                        nextPlayer.getUsername(), cardsToDraw, game.getDrawTwoStackAmount());

                log.debug("Drawing {} cards for player {}", cardsToDraw, nextPlayer.getUsername());
                for (int i = 0; i < cardsToDraw; i++) {
                    Card cardToDraw = game.getDrawDeck().removeFirst();
                    log.trace("Drew card {}/{}: {} {}", 
                            (i+1), cardsToDraw, cardToDraw.getCardColor(), cardToDraw.getCardType());
                    nextPlayer.drawCard(cardToDraw);
                    game.refillDrawDeck();
                }
                log.debug("Player {} now has {} cards", 
                        nextPlayer.getUsername(), nextPlayer.getDeck().size());

                log.debug("Resetting draw two stack amount from {} to 0", game.getDrawTwoStackAmount());
                game.setDrawTwoStackAmount(0); // Reset draw two stack

                log.debug("Skipping player {} who had to draw cards", nextPlayer.getUsername());
                game.setNextPlayerUnconditionally(); // Skip the player who had to draw
            } else {
                log.info("Player {} has a DRAW_TWO card and can play it on their turn", 
                        nextPlayer.getUsername());
            }
        } else if (cardToPlay.getCardType() == CardType.WILD_DRAW_FOUR) {
            log.info("WILD_DRAW_FOUR card played");
            // Move to next player first
            log.debug("Moving to next player to handle WILD_DRAW_FOUR effect");
            //game.setNextPlayerUnconditionally(); OTHER PLAYER WILL CHALLENGE, THEREFORE WE SHOULD MOVE TO THAT

            int nextPlayerIndex = game.getCurrentPlayer();
            Player nextPlayer = game.getPlayers().get(nextPlayerIndex);
            log.debug("Next player is {} (index: {})", nextPlayer.getUsername(), nextPlayerIndex);
            log.debug("Player {} now has {} cards", 
                    nextPlayer.getUsername(), nextPlayer.getDeck().size());

            game.setNextPlayerUnconditionally();
        } else if (cardToPlay.getCardType() != CardType.SKIP && cardToPlay.getCardType() != CardType.REVERSE && cardToPlay.getCardType()!= CardType.WILD_SKIP_EVERYONE_ONCE) {
            //wild skip everyone should keep current player
            // For normal cards, move to next player
            log.debug("Regular card played. Moving to next player");
            game.setNextPlayerUnconditionally();
        }

        // Notify all players of the state change
        log.debug("Sending play card notification to all players in game {}", gameId);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, 
            MultiplayerMessage.builder()
                .type(MultiplayerMessage.MessageType.PLAY_CARD)
                .gameId(gameId)
                .username(username)
                .playerIndex(playerIndex)
                .cardIndex(cardIndex)
                .cardColor(cardColor != null ? cardColor.name() : null)
                .build());
        log.debug("Play card notification sent");

        log.debug("Getting updated game state");
        GameStateDTO gameStateDTO = getGameState(gameId);
        log.debug("Current player after play: {}", gameStateDTO.getCurrentPlayerNo());

        // Check if game has ended
        log.debug("Checking if game has ended");
        if (game.anyPlayerHasEmptyDeck()) {
            log.info("Game {} has ended", gameId);
            String winner = gameStateDTO.getWinner();

            if (winner != null) {
                log.info("Player {} has won the game", winner);

                // Record game in history
                log.debug("Recording win for player {}", winner);
                CreateGameRecordRequest winGameRecordRequest = new CreateGameRecordRequest(
                    winner,
                    1
                );
                gameHistoryService.createGameRecord(winGameRecordRequest);
                log.debug("Win recorded for player {}", winner);

                // Record losses for other players
                log.debug("Recording losses for other players");
                game.getPlayers().stream()
                    .map(Player::getUsername)
                    .filter(p -> !p.equals(winner))
                    .forEach(loser -> {
                        log.debug("Recording loss for player {}", loser);
                        CreateGameRecordRequest loseGameRecordRequest = new CreateGameRecordRequest(
                            loser,
                            -1
                        );
                        gameHistoryService.createGameRecord(loseGameRecordRequest);
                        log.debug("Loss recorded for player {}", loser);
                    }
                );

                // Notify players of game end
                log.debug("Sending game end notification to all players");
                messagingTemplate.convertAndSend("/topic/game/" + gameId,
                    MultiplayerMessage.builder()
                        .type(MultiplayerMessage.MessageType.GAME_END)
                        .gameId(gameId)
                        .content("Game ended. Winner: " + winner)
                        .build());
                log.debug("Game end notification sent");

                // Clean up game resources
                log.debug("Cleaning up game resources");
                cleanupGame(gameId);
                log.info("Game {} resources cleaned up", gameId);
            } else {
                log.warn("Game ended but no winner was determined");
            }
        } else {
            log.debug("Game continues - no player has an empty deck yet");
        }

        log.debug("Returning game state DTO");
        return gameStateDTO;
    }

    /**
     * Leave the multiplayer game
     * @param username The username of the player leaving
     */
    public void leaveGame(String username) {
        log.info("Player {} attempting to leave game", username);

        // Check if player is in a game
        log.debug("Checking if player {} is in a game", username);
        String gameId = playerGameMap.remove(username);
        log.debug("Player-game mapping removed for player {}", username);

        if (gameId != null) {
            log.info("Player {} is leaving game {}", username, gameId);

            // Get the game
            Game game = multiplayerGames.get(gameId);
            if (game != null) {
                log.debug("Game {} found. Has {} players", gameId, game.getPlayers().size());

                // Log player's position in the game
                int playerIndex = -1;
                for (int i = 0; i < game.getPlayers().size(); i++) {
                    if (game.getPlayers().get(i).getUsername().equals(username)) {
                        playerIndex = i;
                        break;
                    }
                }

                if (playerIndex != -1) {
                    log.debug("Player {} was at index {} in game {}", 
                            username, playerIndex, gameId);

                    // Check if it was their turn
                    boolean wasTheirTurn = (playerIndex == game.getCurrentPlayer());
                    log.debug("Was it player {}'s turn? {}", username, wasTheirTurn);
                }
            } else {
                log.debug("Game {} not found in active games map", gameId);
            }

            // Notify other players in the game
            log.debug("Sending room quit notification to all players in game {}", gameId);
            messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                MultiplayerMessage.builder()
                    .type(MultiplayerMessage.MessageType.ROOM_QUIT)
                    .username(username)
                    .gameId(gameId)
                    .content("Player " + username + " left the game")
                    .build());
            log.debug("Room quit notification sent");

            // Clean up game resources
            log.debug("Cleaning up game resources for game {}", gameId);
            cleanupGame(gameId);
            log.info("Game {} resources cleaned up after player {} left", gameId, username);
        } else {
            log.info("Player {} was not in an active game", username);
        }

        // If the player was waiting, remove from waiting list
        log.debug("Checking if player {} was in waiting room", username);
        synchronized (waitingPlayers) {
            boolean wasWaiting = waitingPlayers.contains(username);
            if (wasWaiting) {
                log.info("Removing player {} from waiting room", username);
                waitingPlayers.remove(username);
                log.debug("Player {} removed from waiting room. Remaining waiting players: {}", 
                        username, waitingPlayers.size());
            } else {
                log.debug("Player {} was not in waiting room", username);
            }
        }

        log.info("Player {} has successfully left", username);
    }

    /**
     * Clean up game resources
     * @param gameId The game ID to clean up
     */
    private void cleanupGame(String gameId) {
        log.debug("Starting cleanup for game {}", gameId);

        // Remove game from active games map
        Game game = multiplayerGames.remove(gameId);
        log.debug("Game {} removed from active games map", gameId);
        log.debug("Remaining active games: {}", multiplayerGames.size());

        if (game != null) {
            log.debug("Game {} exists. Cleaning up player mappings", gameId);

            // Log game state before cleanup
            log.debug("Game {} state before cleanup - Players: {}, Current player: {}, Direction: {}", 
                    gameId, 
                    game.getPlayers().size(), 
                    game.getCurrentPlayer(),
                    game.getDirection());

            // Remove all players from the player-game map
            log.debug("Removing all players from player-game map");
            for (Player player : game.getPlayers()) {
                String username = player.getUsername();
                log.trace("Removing mapping for player {}", username);
                playerGameMap.remove(username);
                log.trace("Player {} mapping removed", username);
            }

            log.debug("All player mappings removed for game {}", gameId);
        } else {
            log.warn("Attempted to clean up game {} but it was not found", gameId);
        }

        log.info("Cleanup completed for game {}", gameId);
    }

    public void handleChallenge(String gameId, int playerIndex) {
        if(playerIndex < 0 || playerIndex > 2) {
            throw new IllegalArgumentException("Invalid player index");
        }
        Game game = multiplayerGames.get(gameId);
        if(!game.isCanChallenge()){
            throw new IllegalArgumentException("Cannot challenge now");
        }
        if(game.getCurrentPlayer() != playerIndex){
            throw new IllegalArgumentException("Not your turn to challenge");
        }
        Player otherPlayer;
        if(playerIndex==0)
            otherPlayer = game.getPlayers().get(1);
        else
            otherPlayer = game.getPlayers().get(0);
        List<Card> wildFourExcludedValidCards = otherPlayer.getDeck().stream()
                .filter(card -> card.getCardType() != CardType.WILD_DRAW_FOUR)
                .filter(card -> CardUtil.isValidMove(card, game.getLastPlayedCardBeforeWildDrawFour(), game.getLastPlayedCardBeforeWildDrawFour().getCardColor()))
                .toList();
        if (!wildFourExcludedValidCards.isEmpty()) {
            System.out.println("Other playable card: " + wildFourExcludedValidCards.get(0).getCardType() + "/" + wildFourExcludedValidCards.get(0).getCardColor());
            //challenged player has a card to play actually, let other player draw card
            for(int i = 0; i < 4; i++){
                Card cardToDraw = game.getDrawDeck().removeFirst();
                otherPlayer.drawCard(cardToDraw);
            }
            if(playerIndex == 0)
                System.out.println("opponent player: 1 draw 4 cards, lost the challenge");
            else
                System.out.println("opponent player: 0 draw 4 cards, lost the challenge");
        }
        else{
            Player player = game.getPlayers().get(playerIndex);
            for(int i = 0; i < 6; i++){
                Card cardToDraw = game.getDrawDeck().removeFirst();
                player.drawCard(cardToDraw);
            }
            System.out.println("current player: " + playerIndex + " draw 6 cards, lost the challenge");
            game.setNextPlayerUnconditionally(); //since we drawed, we lose our turn
        }
        game.setCanChallenge(false);
        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                MultiplayerMessage.builder()
                        .type(MultiplayerMessage.MessageType.CHALLENGE)
                        .gameId(gameId)
                        .playerIndex(playerIndex)
                        .build());
    }

    public void rejectChallenge(String gameId, int playerIndex) {
        if(playerIndex < 0 || playerIndex > 2) {
            throw new IllegalArgumentException("Invalid player index");
        }
        Game game = multiplayerGames.get(gameId);
        if(game.getCurrentPlayer() != playerIndex){
            throw new IllegalArgumentException("Not your turn to challenge");
        }
        Player player = game.getPlayers().get(playerIndex);
        for(int i = 0; i < 4; i++){
            Card cardToDraw = game.getDrawDeck().removeFirst();
            player.drawCard(cardToDraw);
        }
        System.out.println("Rejected challenge and drawn 4 cards by player index: " + playerIndex);
        game.setCanChallenge(false);
        game.setNextPlayerUnconditionally(); //since we drawed, we lose our turn
        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                MultiplayerMessage.builder()
                        .type(MultiplayerMessage.MessageType.REJECT_CHALLENGE)
                        .gameId(gameId)
                        .playerIndex(playerIndex)
                        .build());
    }
}
