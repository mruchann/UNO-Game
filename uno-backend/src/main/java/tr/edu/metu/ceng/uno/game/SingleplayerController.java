package tr.edu.metu.ceng.uno.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.metu.ceng.uno.card.CardColor;
import tr.edu.metu.ceng.uno.dto.GameStateDTO;

@RestController
@RequestMapping("/api/singleplayer")
@Slf4j
public class SingleplayerController {


    private final SingleplayerGameService singleplayerGameService;

    @Autowired
    public SingleplayerController(SingleplayerGameService singleplayerGameService) {
        this.singleplayerGameService = singleplayerGameService;
        log.info("SingleplayerController initialized");
    }

    /**
     * Endpoint to create a new UNO singleplayer game
     * 
     * @param username The username of the human player
     * @param computerPlayerCount Number of computer players to add
     * @return GameStateDTO containing the initial game state
     */
    @PostMapping("/start")
    public ResponseEntity<GameStateDTO> startSingleplayerGame(
            @RequestParam String username,
            @RequestParam(defaultValue = "3") int computerPlayerCount) {

        log.info("Starting new singleplayer game for player {} with {} computer players", 
                username, computerPlayerCount);

        try {
            GameStateDTO gameState = singleplayerGameService.createSingleplayerGame(username, computerPlayerCount);
            log.info("Singleplayer game started successfully for player {}", username);
            log.debug("Initial game state: current player: {}, players count: {}", 
                    gameState.getCurrentPlayerNo(), gameState.getPlayers().size());
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error starting singleplayer game for player {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * Endpoint to get the current singleplayer game state for a player
     * 
     * @param username The username of the player requesting the game state
     * @return GameStateDTO containing the current game state
     */
    @GetMapping("/state")
    public ResponseEntity<GameStateDTO> getSingleplayerGameState(@RequestParam String username) {
        log.debug("Getting game state for singleplayer game for player {}", username);

        try {
            GameStateDTO gameState = singleplayerGameService.getSingleplayerGameState();
            log.debug("Game state retrieved for player {}. Current player: {}, Players count: {}", 
                    username, gameState.getCurrentPlayerNo(), gameState.getPlayers().size());
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error getting game state for player {}: {}", username, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/play-card-computer")
    public ResponseEntity<GameStateDTO> playComputerCard(@RequestParam int playerId) {
        log.info("Computer player {} playing card", playerId);

        try {
            GameStateDTO gameState = singleplayerGameService.playComputerCard(playerId);
            log.debug("Computer player {} played card successfully", playerId);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error playing card for computer player {}: {}", playerId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/play-card-human")
    public ResponseEntity<GameStateDTO> playHumanCard(
            @RequestParam int playerId, 
            @RequestParam int cardIndex, 
            @RequestParam(defaultValue = "NONE") CardColor cardColor) {

        log.info("Human player {} playing card {} with color {}", playerId, cardIndex, cardColor);

        try {
            GameStateDTO gameState = singleplayerGameService.playHumanCard(playerId, cardIndex, cardColor);
            log.debug("Human player {} played card {} successfully", playerId, cardIndex);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error playing card {} for human player {}: {}", cardIndex, playerId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/draw-card")
    public ResponseEntity<GameStateDTO> drawCardSingleplayer(@RequestParam int playerId) {
        log.info("Player {} drawing card", playerId);

        try {
            GameStateDTO gameState = singleplayerGameService.drawCardSingleplayer(playerId);
            log.debug("Player {} drew card successfully", playerId);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error drawing card for player {}: {}", playerId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/cheat-card")
    public ResponseEntity<GameStateDTO> useCheatCard(@RequestParam int playerId, @RequestParam String cheatType) {
        log.info("Player {} using cheat card of type {}", playerId, cheatType);

        try {
            GameStateDTO gameState = singleplayerGameService.useCheatCard(playerId, cheatType);
            log.debug("Player {} used cheat card {} successfully", playerId, cheatType);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Error using cheat card {} for player {}: {}", cheatType, playerId, e.getMessage());
            throw e;
        }
    }
}
