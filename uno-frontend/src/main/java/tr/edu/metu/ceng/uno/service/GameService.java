package tr.edu.metu.ceng.uno.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tr.edu.metu.ceng.uno.controller.SingleplayerGameViewController;
import tr.edu.metu.ceng.uno.model.GameStateDTO;

@Service
@Slf4j
public class GameService {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private final AuthService authService;
    private SingleplayerGameViewController singlePlayerGameViewController;
    private GameStateDTO currentGameState;

    public GameService(RestTemplate restTemplate, AuthService authService, @Value("${api.base-url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
        this.authService = authService;
    }

    /**
     * Sets the MainViewController reference to enable UI updates
     * 
     * @param singlePlayerGameViewController The MainViewController instance
     */
    public void setMainViewController(SingleplayerGameViewController singlePlayerGameViewController) {
        log.debug("Setting SingleplayerGameViewController reference");
        this.singlePlayerGameViewController = singlePlayerGameViewController;
    }

    /**
     * Fetches the game state from the backend
     * 
     * @return GameStateDTO containing player decks and game information
     */
    public GameStateDTO startGame() {
        String username = authService.getCurrentUsername();
        log.info("Starting new singleplayer game for user: {}", username);

        String url = apiBaseUrl + "/api/singleplayer/start";
        log.debug("Sending request to: {}", url);

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Setting up form parameters with the current username instead of hardcoded value
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("computerPlayerCount", "3");

        //HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // Sending POST request with parameters
            log.debug("Sending POST request to start game");
            ResponseEntity<GameStateDTO> response = restTemplate.postForEntity(url, request, GameStateDTO.class);
            currentGameState = response.getBody();

            if (currentGameState != null) {
                log.info("Game started successfully with {} players", 
                        currentGameState.getPlayers() != null ? currentGameState.getPlayers().size() : 0);
            } else {
                log.warn("Received null game state from server");
            }

            return currentGameState;
        } catch (Exception e) {
            log.error("Error starting game: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sends a playCard request to the backend and updates the UI with the response
     * 
     * @param playerIndex The index of the player playing the card
     */
    public void playCardComputer(int playerIndex) {
        log.info("Computer player {} is playing a card", playerIndex);
        String PLAY_CARD_URL = apiBaseUrl + "/api/singleplayer/play-card-computer";
        log.debug("Sending request to: {}", PLAY_CARD_URL);

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Setting up form parameters with dummy values
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("playerId", String.valueOf(playerIndex));

        //HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // Sending POST request with parameters
            log.debug("Sending POST request for computer to play card");
            ResponseEntity<GameStateDTO> response = restTemplate.postForEntity(PLAY_CARD_URL, request, GameStateDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Computer player {} played card successfully", playerIndex);

                // Update the UI with the new game state
                currentGameState = response.getBody();
                if (currentGameState != null && singlePlayerGameViewController != null) {
                    log.debug("Refreshing UI with updated game state");
                    singlePlayerGameViewController.refreshUI(currentGameState);
                } else if (currentGameState == null) {
                    log.warn("Received null game state from server");
                } else if (singlePlayerGameViewController == null) {
                    log.warn("SingleplayerGameViewController reference is null");
                }
            } else {
                log.error("Failed to play card: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error playing computer card: {}", e.getMessage(), e);
        }
    }

    public void drawCardHuman(int playerIndex) {
        log.info("Human player {} is drawing a card", playerIndex);
        String DRAW_CARD_URL = apiBaseUrl + "/api/singleplayer/draw-card";
        log.debug("Sending request to: {}", DRAW_CARD_URL);

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Setting up form parameters with dummy values
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("playerId", String.valueOf(playerIndex));

        //HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // Sending POST request with parameters
            log.debug("Sending POST request to draw card");
            ResponseEntity<GameStateDTO> response = restTemplate.postForEntity(DRAW_CARD_URL, request, GameStateDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Human player {} drew card successfully", playerIndex);

                // Update the UI with the new game state
                currentGameState = response.getBody();
                if (currentGameState != null && singlePlayerGameViewController != null) {
                    log.debug("Refreshing UI with updated game state");
                    singlePlayerGameViewController.refreshUI(currentGameState);
                } else if (currentGameState == null) {
                    log.warn("Received null game state from server");
                } else if (singlePlayerGameViewController == null) {
                    log.warn("SingleplayerGameViewController reference is null");
                }
            } else {
                log.error("Failed to draw card: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error drawing card: {}", e.getMessage(), e);
        }
    }

    public void playCardHuman(int playerIndex, int cardIndex, String selectedColor) {
        if (selectedColor != null) {
            log.info("Human player {} is playing card {} with color {}", playerIndex, cardIndex, selectedColor);
        } else {
            log.info("Human player {} is playing card {}", playerIndex, cardIndex);
        }

        String PLAY_CARD_URL = apiBaseUrl + "/api/singleplayer/play-card-human";
        log.debug("Sending request to: {}", PLAY_CARD_URL);

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Setting up form parameters with player ID, card index, and selected color
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("playerId", String.valueOf(playerIndex));
        map.add("cardIndex", String.valueOf(cardIndex));

        // Only add selectedColor parameter if it's not null (for wild cards)
        if (selectedColor != null) {
            map.add("cardColor", selectedColor);
        }

        //HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // Sending POST request with parameters
            log.debug("Sending POST request to play human card");
            ResponseEntity<GameStateDTO> response = restTemplate.postForEntity(PLAY_CARD_URL, request, GameStateDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                if (selectedColor != null) {
                    log.info("Wild card played successfully with color: {}", selectedColor);
                } else {
                    log.info("Card played successfully");
                }

                // Update the UI with the new game state
                currentGameState = response.getBody();
                if (currentGameState != null && singlePlayerGameViewController != null) {
                    log.debug("Refreshing UI with updated game state");
                    singlePlayerGameViewController.refreshUI(currentGameState);
                } else if (currentGameState == null) {
                    log.warn("Received null game state from server");
                } else if (singlePlayerGameViewController == null) {
                    log.warn("SingleplayerGameViewController reference is null");
                }
            } else {
                log.error("Failed to play card: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error playing human card: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends a cheat card request to the backend
     * 
     * @param cheatType The type of cheat card to use
     */
    public void useCheatCard(String cheatType) {
        log.info("Human player is using cheat card: {}", cheatType);
        String CHEAT_CARD_URL = apiBaseUrl + "/api/singleplayer/cheat-card";
        log.debug("Sending request to: {}", CHEAT_CARD_URL);

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Setting up form parameters
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("playerId", "0"); // Human player is always at index 0
        map.add("cheatType", cheatType);

        // HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // Sending POST request with parameters
            log.debug("Sending POST request to use cheat card");
            ResponseEntity<GameStateDTO> response = restTemplate.postForEntity(CHEAT_CARD_URL, request, GameStateDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Cheat {} card used successfully", cheatType);

                currentGameState = response.getBody();
                if (currentGameState != null && singlePlayerGameViewController != null) {
                    log.debug("Refreshing UI with updated game state");
                    singlePlayerGameViewController.refreshUI(currentGameState);
                } else if (currentGameState == null) {
                    log.warn("Received null game state from server");
                } else if (singlePlayerGameViewController == null) {
                    log.warn("SingleplayerGameViewController reference is null");
                }
            } else {
                log.error("Failed to use cheat {} card: {}", cheatType, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error using cheat card: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns the most recently received game state
     * 
     * @return The current game state, or null if no game state has been received yet
     */
    public GameStateDTO getCurrentGameState() {
        log.debug("Getting current game state");
        return currentGameState;
    }
}
