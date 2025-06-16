package tr.edu.metu.ceng.uno.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.edu.metu.ceng.uno.card.CardColor;
import tr.edu.metu.ceng.uno.dto.GameStateDTO;
import tr.edu.metu.ceng.uno.websocket.MultiplayerMessage;

@RestController
@RequestMapping("/api/multiplayer")
@Slf4j
public class MultiplayerController {

    private final MultiplayerGameService multiplayerGameService;
    private final SimpMessagingTemplate messagingTemplate;

    public MultiplayerController(MultiplayerGameService multiplayerGameService, SimpMessagingTemplate messagingTemplate) {
        this.multiplayerGameService = multiplayerGameService;
        this.messagingTemplate = messagingTemplate;
        log.info("MultiplayerController initialized");
    }

    /**
     * REST endpoint to join a multiplayer room
     *
     * @param username Player username
     * @return gameId if a game has started, or a waiting message
     */
    @GetMapping("/join/{username}")
    public ResponseEntity<String> joinRoom(@PathVariable String username) {
        log.info("REST API: Player {} attempting to join multiplayer room", username);

        String gameId = multiplayerGameService.joinRoom(username);

        if (gameId != null) {
            log.info("Player {} joined game with ID: {}", username, gameId);
            return ResponseEntity.ok(gameId);
        } else {
            log.info("Player {} added to waiting room", username);
            return ResponseEntity.ok("waiting");
        }
    }

    /**
     * REST endpoint to get game state for a specific game
     *
     * @param gameId The game ID
     * @return GameStateDTO with the current game state
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<GameStateDTO> getGameState(@PathVariable String gameId) {
        log.debug("REST API: Getting game state for game ID: {}", gameId);

        GameStateDTO gameState = multiplayerGameService.getGameState(gameId);

        log.debug("Game state retrieved for game ID: {}. Current player: {}, Players count: {}", 
                gameId, gameState.getCurrentPlayerNo(), gameState.getPlayers().size());

        return ResponseEntity.ok(gameState);
    }

    /**
     * WebSocket endpoint to handle player joining a room
     *
     * @param message The join message
     * @return Updated message to broadcast
     */
    @MessageMapping("/room/join")
    @SendTo("/topic/lobby")
    public MultiplayerMessage handleJoinRoom(MultiplayerMessage message) {
        String username = message.getUsername();
        log.info("WebSocket: Player {} attempting to join multiplayer room", username);

        String gameId = multiplayerGameService.joinRoom(username);

        // If game started immediately, the notification is already sent by the service
        // Just return a simple confirmation that the room join was processed
        message.setType(MultiplayerMessage.MessageType.ROOM_JOIN);
        message.setContent("Player " + username + " joined the waiting room");

        log.info("WebSocket: Player {} join request processed", username);
        return message;
    }

    /**
     * WebSocket endpoint to handle player leaving a room
     *
     * @param message The leave message
     * @return Updated message to broadcast
     */
    @MessageMapping("/room/leave")
    @SendTo("/topic/lobby")
    public MultiplayerMessage handleLeaveRoom(MultiplayerMessage message) {
        String username = message.getUsername();
        log.info("WebSocket: Player {} leaving room", username);

        multiplayerGameService.leaveGame(username);
        message.setType(MultiplayerMessage.MessageType.ROOM_QUIT);
        message.setContent("Player " + username + " left the room");

        log.info("WebSocket: Player {} has left the room", username);
        return message;
    }

    /**
     * WebSocket endpoint to handle card drawing
     *
     * @param message The draw card message
     */
    @MessageMapping("/game/draw")
    public void handleDrawCard(MultiplayerMessage message) {
        log.info("WebSocket: Player {} (index: {}) drawing card in game {}", 
                message.getUsername(), message.getPlayerIndex(), message.getGameId());

        try {
            multiplayerGameService.drawCard(
                message.getGameId(),
                message.getUsername(),
                message.getPlayerIndex()
            );
            log.debug("WebSocket: Card draw successful for player {} in game {}", 
                    message.getUsername(), message.getGameId());
        } catch (Exception e) {
            log.error("WebSocket: Error drawing card for player {} in game {}: {}", 
                    message.getUsername(), message.getGameId(), e.getMessage());
            throw e;
        }

        // Notification is sent by the service
    }

    /**
     * WebSocket endpoint to handle card playing
     *
     * @param message The play card message
     */
    @MessageMapping("/game/play")
    public void handlePlayCard(MultiplayerMessage message) {
        CardColor cardColor = null;
        if (message.getCardColor() != null && !message.getCardColor().isEmpty()) {
            cardColor = CardColor.valueOf(message.getCardColor());
        }

        log.info("WebSocket: Player {} (index: {}) playing card {} with color {} in game {}", 
                message.getUsername(), message.getPlayerIndex(), message.getCardIndex(), 
                cardColor, message.getGameId());

        try {
            multiplayerGameService.playCard(
                message.getGameId(),
                message.getUsername(),
                message.getPlayerIndex(),
                message.getCardIndex(),
                cardColor
            );
            log.debug("WebSocket: Card play successful for player {} in game {}", 
                    message.getUsername(), message.getGameId());
        } catch (Exception e) {
            log.error("WebSocket: Error playing card for player {} in game {}: {}", 
                    message.getUsername(), message.getGameId(), e.getMessage());
            throw e;
        }

        // Notification is sent by the service
    }

    @MessageMapping("/game/challenge")
    public void handleChallenge(MultiplayerMessage message) {
        multiplayerGameService.handleChallenge(message.getGameId(), message.getPlayerIndex());
    }

    @MessageMapping("/game/reject-challenge")
    public void rejectChallenge(MultiplayerMessage message) {
        multiplayerGameService.rejectChallenge(message.getGameId(), message.getPlayerIndex());
    }


} 
