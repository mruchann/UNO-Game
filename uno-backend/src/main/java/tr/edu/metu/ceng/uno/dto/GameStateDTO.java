package tr.edu.metu.ceng.uno.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tr.edu.metu.ceng.uno.game.Game;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for transferring complete game state to the frontend
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameStateDTO {
    private List<PlayerDTO> players;
    private int currentPlayerNo;
    private CardDTO lastPlayedCard;
    private String currentColor;
    private String direction;
    private int drawDeckSize;
    private boolean gameFinished; //TODO: may not need
    private String winner; //TODO: may not need
    private boolean canPlayWildDrawFour; // Indicates if the current player can play a Wild Draw Four card
    private boolean canChallenge;
    
    /**
     * Converts a Game entity to GameStateDTO from a specific player's perspective
     * 
     * @param game The game entity to convert
     * @return GameStateDTO representation of the game state
     */
    public static GameStateDTO createGameStateDTO(Game game) {
        GameStateDTO dto = new GameStateDTO();

        dto.setDirection(game.getDirection().toString());
        dto.setDrawDeckSize(game.getDrawDeck().size());
        dto.setLastPlayedCard(CardDTO.createCardDTO(game.getLastPlayedCard()));
        dto.setCurrentColor(game.getCurrentCardColor().toString());
        dto.setGameFinished(game.anyPlayerHasEmptyDeck());
        dto.setCanChallenge(game.isCanChallenge());

        List<PlayerDTO> playerDTOs = game.getPlayers().stream()
                .map(PlayerDTO::createPLayerDTOForCurrentPlayer)
                .collect(Collectors.toList());
        
        dto.setPlayers(playerDTOs);
        
        // Set current player
        int currentPlayerIndex = game.getCurrentPlayer();
        dto.setCurrentPlayerNo(currentPlayerIndex);
        
        // Calculate if current player can play a Wild Draw Four card
        // (true if they have no other playable cards except for Wild Draw Four)
        boolean canPlayWildDrawFour = false;
        if (!game.anyPlayerHasEmptyDeck()) {
            var currentPlayer = game.getPlayers().get(currentPlayerIndex);
            // Check if the player has any valid non-Wild-Draw-Four cards to play
            var hasNonWildDrawFourValidCards = currentPlayer.getDeck().stream()
                .filter(card -> card.getCardType() != tr.edu.metu.ceng.uno.card.CardType.WILD_DRAW_FOUR)
                .anyMatch(card -> tr.edu.metu.ceng.uno.util.CardUtil.isValidMove(card, game.getLastPlayedCard(), game.getCurrentCardColor()));
                
            // Can play Wild Draw Four only if there are no other valid cards to play
            canPlayWildDrawFour = !hasNonWildDrawFourValidCards;
        }
        dto.setCanPlayWildDrawFour(canPlayWildDrawFour);
        
        // Set winner if game is finished
        if (dto.isGameFinished()) {
            game.getPlayers().stream()
                .filter(player -> player.getDeck().isEmpty())
                .findFirst()
                .ifPresent(winner -> dto.setWinner(winner.getUsername()));
        }
        
        return dto;
    }
}