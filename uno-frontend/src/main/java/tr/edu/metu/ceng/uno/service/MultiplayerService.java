package tr.edu.metu.ceng.uno.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import tr.edu.metu.ceng.uno.model.GameStateDTO;
import tr.edu.metu.ceng.uno.model.MultiplayerMessage;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Service
@Slf4j
public class MultiplayerService {

    private final RestTemplate restTemplate;
    private final AuthService authService;
    private final String apiBaseUrl;
    private final String wsBaseUrl;

    private StompSession stompSession;
    private String gameId;
    private Consumer<MultiplayerMessage> messageHandler;
    private Consumer<GameStateDTO> gameStateHandler;

    public MultiplayerService(
            RestTemplate restTemplate, 
            AuthService authService, 
            @Value("${api.base-url}") String apiBaseUrl,
            @Value("${ws.base-url:ws://localhost:8080}") String wsBaseUrl) {
        log.info("Initializing MultiplayerService");
        this.restTemplate = restTemplate;
        this.authService = authService;
        this.apiBaseUrl = apiBaseUrl;
        this.wsBaseUrl = wsBaseUrl;
        log.debug("MultiplayerService initialized with API URL: {}, WebSocket URL: {}", apiBaseUrl, wsBaseUrl);
    }

    /**
     * Connect to the WebSocket server
     */
    public void connect() {
        if (stompSession != null && stompSession.isConnected()) {
            log.debug("Already connected to WebSocket server, skipping connection");
            return; // Already connected
        }

        log.info("Connecting to WebSocket server at {}", wsBaseUrl);
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            StompSessionHandler sessionHandler = new StompSessionHandler() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    log.info("Connected to WebSocket server successfully");

                    // Subscribe to the lobby topic
                    log.debug("Subscribing to lobby topic");
                    session.subscribe("/topic/lobby", this);
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    log.error("Error in WebSocket communication: {}", exception.getMessage(), exception);
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    log.error("Transport error in WebSocket communication: {}", exception.getMessage(), exception);
                }

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return MultiplayerMessage.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    if (payload instanceof MultiplayerMessage) {
                        MultiplayerMessage message = (MultiplayerMessage) payload;
                        log.debug("Received message on lobby topic: {}, gameId: {}", message.getType(), message.getGameId());

                        // If game is starting, subscribe to game-specific topic
                        if (message.getType() == MultiplayerMessage.MessageType.GAME_START && message.getGameId() != null) {
                            log.info("Game starting! Subscribing to game: {}", message.getGameId());
                            gameId = message.getGameId();
                            subscribeToGame(message.getGameId());
                        }

                        if (messageHandler != null) {
                            messageHandler.accept(message);
                        }
                    }
                }
            };

            stompSession = stompClient.connect(wsBaseUrl + "/ws-uno", sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to connect to WebSocket server: {}", e.getMessage(), e);
        }
    }

    /**
     * Subscribe to game-specific messages
     * 
     * @param gameId The game ID to subscribe to
     */
    public void subscribeToGame(String gameId) {
        this.gameId = gameId;

        if (stompSession != null && stompSession.isConnected()) {
            log.info("Subscribing to game topic for game ID: {}", gameId);
            stompSession.subscribe("/topic/game/" + gameId, new StompSessionHandler() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    // Not used here
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    log.error("Error in game WebSocket communication: {}", exception.getMessage(), exception);
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    log.error("Transport error in game WebSocket communication: {}", exception.getMessage(), exception);
                }

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return MultiplayerMessage.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    if (payload instanceof MultiplayerMessage) {
                        MultiplayerMessage message = (MultiplayerMessage) payload;
                        log.debug("Received game message: {}, gameId: {}", message.getType(), message.getGameId());

                        if (messageHandler != null) {
                            messageHandler.accept(message);
                        }

                        // If there's a state change, refresh the game state
                        if (message.getType() == MultiplayerMessage.MessageType.PLAY_CARD || 
                            message.getType() == MultiplayerMessage.MessageType.DRAW_CARD ||
                            message.getType() == MultiplayerMessage.MessageType.CHALLENGE ||
                            message.getType() == MultiplayerMessage.MessageType.REJECT_CHALLENGE) {
                            System.out.println("State change detected for type: " + message.getType());
                            log.debug("Game state change detected, refreshing game state");
                            refreshGameState();
                        }
                        
                        // Handle player quit messages specifically
                        if (message.getType() == MultiplayerMessage.MessageType.ROOM_QUIT) {
                            log.info("Player {} has quit the game", message.getUsername());
                            // Check if it's not the current user who quit
                            if (!message.getUsername().equals(authService.getCurrentUsername())) {
                                if (gameStateHandler != null) {
                                    log.debug("Notifying game state handler of player quit");
                                    GameStateDTO gameState = new GameStateDTO();
                                    gameState.setQuittingPlayerName(message.getUsername());
                                    gameStateHandler.accept(gameState);
                                }
                            }
                        }
                    }
                }
            });
        } else {
            log.warn("Cannot subscribe to game: not connected to WebSocket server");
        }
    }

    /**
     * Join a multiplayer game room
     * 
     * @return CompletableFuture that completes when the game starts with the game ID, or "waiting" if still waiting
     */
    public CompletableFuture<String> joinRoom() {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Make sure we're connected to WebSocket server
        connect();

        String username = authService.getCurrentUsername();
        log.info("User {} is joining multiplayer room", username);

        // Set handler for when we receive a game start message BEFORE sending join request
        // This ensures we don't miss any messages
        setMessageHandler(msg -> {
            log.debug("Message received in handler: {}", msg.getType());
            if (msg.getType() == MultiplayerMessage.MessageType.GAME_START && !future.isDone()) {
                log.info("Game start received for game: {}", msg.getGameId());
                this.gameId = msg.getGameId();
                future.complete(msg.getGameId());
            }
        });

        // Send a REST request to join the room
        String joinUrl = apiBaseUrl + "/api/multiplayer/join/" + username;
        log.debug("Sending REST request to join room: {}", joinUrl);
        String response = restTemplate.getForObject(joinUrl, String.class);

        if (response != null && !response.equals("waiting")) {
            // Game started immediately (second player case)
            log.info("Game started immediately with ID: {}", response);
            this.gameId = response;
            subscribeToGame(response);
            future.complete(response);
        } else {
            // Still waiting (first player case)
            log.info("Waiting for another player to join the room");
            MultiplayerMessage message = new MultiplayerMessage();
            message.setType(MultiplayerMessage.MessageType.ROOM_JOIN);
            message.setUsername(username);

            log.debug("Sending WebSocket message to join room");
            stompSession.send("/app/room/join", message);
        }

        return future;
    }

    /**
     * Leave the current multiplayer game
     */
    public void leaveGame() {
        if (stompSession != null && stompSession.isConnected()) {
            String username = authService.getCurrentUsername();
            log.info("User {} is leaving game {}", username, gameId);

            MultiplayerMessage message = new MultiplayerMessage();
            message.setType(MultiplayerMessage.MessageType.ROOM_QUIT);
            message.setUsername(username);
            message.setGameId(gameId);

            log.debug("Sending WebSocket message to leave game");
            stompSession.send("/app/room/leave", message);
        } else {
            log.warn("Cannot leave game: not connected to WebSocket server");
        }

        disconnect();
        log.debug("Disconnected from multiplayer service");
    }

    /**
     * Draw a card in multiplayer mode
     * 
     * @param playerIndex The index of the player drawing a card
     */
    public void drawCard(int playerIndex) {
        if (stompSession != null && stompSession.isConnected() && gameId != null) {
            String username = authService.getCurrentUsername();
            log.info("User {} (player {}) is drawing a card in game {}", username, playerIndex, gameId);

            MultiplayerMessage message = new MultiplayerMessage();
            message.setType(MultiplayerMessage.MessageType.DRAW_CARD);
            message.setUsername(username);
            message.setGameId(gameId);
            message.setPlayerIndex(playerIndex);

            log.debug("Sending WebSocket message to draw card");
            stompSession.send("/app/game/draw", message);
        } else {
            log.warn("Cannot draw card: not connected to WebSocket server or no active game");
        }
    }

    /**
     * Play a card in multiplayer mode
     * 
     * @param playerIndex The index of the player playing a card
     * @param cardIndex The index of the card being played
     * @param cardColor The color selected for wild cards (can be null)
     */
    public void playCard(int playerIndex, int cardIndex, String cardColor) {
        if (stompSession != null && stompSession.isConnected() && gameId != null) {
            String username = authService.getCurrentUsername();
            log.info("User {} (player {}) is playing card {} {} in game {}", 
                    username, playerIndex, cardIndex, 
                    cardColor != null ? "with color " + cardColor : "", gameId);

            MultiplayerMessage message = new MultiplayerMessage();
            message.setType(MultiplayerMessage.MessageType.PLAY_CARD);
            message.setUsername(username);
            message.setGameId(gameId);
            message.setPlayerIndex(playerIndex);
            message.setCardIndex(cardIndex);
            message.setCardColor(cardColor);

            log.debug("Sending WebSocket message to play card");
            stompSession.send("/app/game/play", message);
        } else {
            log.warn("Cannot play card: not connected to WebSocket server or no active game");
        }
    }

    /**
     * Challenge a Wild Draw Four card in multiplayer mode
     * 
     * @param playerIndex The index of the player issuing the challenge
     */
    public void challengeWildDrawFour(int playerIndex) {
        if (stompSession != null && stompSession.isConnected() && gameId != null) {
            String username = authService.getCurrentUsername();

            MultiplayerMessage message = new MultiplayerMessage();
            message.setType(MultiplayerMessage.MessageType.PLAY_CARD);
            message.setUsername(username);
            message.setGameId(gameId);
            message.setPlayerIndex(playerIndex);

            log.debug("Sending WebSocket message to challenge");
            stompSession.send("/app/game/challenge", message);
        } else {
            log.warn("Cannot challenge: not connected to WebSocket server or no active game");
        }
    }

    /**
     * Reject a Wild Draw Four card challenge in multiplayer mode
     * 
     * @param playerIndex The index of the player rejecting the challenge
     */
    public void rejectWildDrawFourChallenge(int playerIndex) {
        if (stompSession != null && stompSession.isConnected() && gameId != null) {
            String username = authService.getCurrentUsername();
            log.info("User {} (player {}) is rejecting the Wild Draw Four challenge in game {}", 
                    username, playerIndex, gameId);

            MultiplayerMessage message = new MultiplayerMessage();
            message.setType(MultiplayerMessage.MessageType.REJECT_CHALLENGE);
            message.setUsername(username);
            message.setGameId(gameId);
            message.setPlayerIndex(playerIndex);

            log.debug("Sending WebSocket message to reject challenge");
            stompSession.send("/app/game/reject-challenge", message);
        } else {
            log.warn("Cannot reject challenge: not connected to WebSocket server or no active game");
        }
    }

    /**
     * Get the current game state for the current game
     * 
     * @return The current game state
     */
    public GameStateDTO getGameState() {
        if (gameId == null) {
            log.warn("Cannot get game state: no active game");
            return null;
        }

        String url = apiBaseUrl + "/api/multiplayer/game/" + gameId;
        log.debug("Fetching game state from: {}", url);
        try {
            GameStateDTO gameState = restTemplate.getForObject(url, GameStateDTO.class);
            if (gameState == null) {
                log.warn("Received null game state from server");
            }
            return gameState;
        } catch (Exception e) {
            log.error("Error fetching game state: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Refresh the game state and notify any listeners
     */
    public void refreshGameState() {
        log.debug("Refreshing game state for game: {}", gameId);
        GameStateDTO gameState = getGameState();

        if (gameState != null && gameStateHandler != null) {
            log.debug("Notifying game state handler of updated state");
            gameStateHandler.accept(gameState);
        } else if (gameState == null) {
            log.warn("Cannot refresh game state: received null game state");
        } else if (gameStateHandler == null) {
            log.warn("Cannot refresh game state: no game state handler registered");
        }
    }

    /**
     * Set a handler for WebSocket messages
     * 
     * @param handler The handler to receive messages
     */
    public void setMessageHandler(Consumer<MultiplayerMessage> handler) {
        log.debug("Setting message handler");
        this.messageHandler = handler;
    }

    /**
     * Set a handler for game state updates
     * 
     * @param handler The handler to receive game state updates
     */
    public void setGameStateHandler(Consumer<GameStateDTO> handler) {
        log.debug("Setting game state handler");
        this.gameStateHandler = handler;
    }

    /**
     * Get the current game ID
     * 
     * @return The current game ID
     */
    public String getGameId() {
        log.debug("Getting game ID: {}", gameId);
        if (gameId == null) {
            log.debug("Game ID is null, no active game");
        }
        return gameId;
    }

    /**
     * Disconnect from the WebSocket server
     */
    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            log.info("Disconnecting from WebSocket server");
            stompSession.disconnect();
            stompSession = null;
            gameId = null;
            log.debug("WebSocket connection closed");
        } else {
            log.debug("No active WebSocket connection to disconnect");
        }
    }
}
