package tr.edu.metu.ceng.uno.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tr.edu.metu.ceng.uno.card.*;
import tr.edu.metu.ceng.uno.dto.GameStateDTO;
import tr.edu.metu.ceng.uno.history.GameHistoryService;
import tr.edu.metu.ceng.uno.history.request.CreateGameRecordRequest;
import tr.edu.metu.ceng.uno.player.ComputerPlayer;
import tr.edu.metu.ceng.uno.player.HumanPlayer;
import tr.edu.metu.ceng.uno.player.Player;
import tr.edu.metu.ceng.uno.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class SingleplayerGameService {

    // Single game instance for now
    private Game currentGame;
    private final GameHistoryService gameHistoryService;

    public SingleplayerGameService(GameHistoryService gameHistoryService) {
        this.gameHistoryService = gameHistoryService;
    }

    /**
     * Creates a new UNO game with the specified human player and computer players
     * 
     * @param username The username of the human player
     * @param computerPlayerCount Number of computer players to add (1-3)
     * @return GameStateDTO containing the initial game state
     */
    public GameStateDTO createSingleplayerGame(String username, int computerPlayerCount) {
        if (computerPlayerCount < 1 || computerPlayerCount > 3) {
            throw new IllegalArgumentException("Computer player count must be between 1 and 3");
        }

        List<Player> players = new ArrayList<>();

        players.add(new HumanPlayer(username));

        for (int i = 1; i <= computerPlayerCount; i++) {
            players.add(new ComputerPlayer("Computer " + i));
        }

        currentGame = new Game(players);

        currentGame.distributeCards();
        currentGame.initializeFirstCard();

        // Create and return the game state DTO for the human player
        return GameStateDTO.createGameStateDTO(currentGame);
    }

    /**
     * Gets the current game state for a specific player
     * 
     * @return GameStateDTO containing the current game state
     */
    public GameStateDTO getSingleplayerGameState() {
        if (currentGame == null) {
            throw new IllegalStateException("No game is currently active");
        }

        GameStateDTO gameStateDTO = GameStateDTO.createGameStateDTO(currentGame);
        if (getCurrentGame().anyPlayerHasEmptyDeck() && currentGame.getPlayers().getFirst().getUsername().equals(gameStateDTO.getWinner())) {
            CreateGameRecordRequest createGameRecordRequest = new CreateGameRecordRequest(
                currentGame.getPlayers().getFirst().getUsername(),
                1
            );
            gameHistoryService.createGameRecord(createGameRecordRequest);
        }

        return gameStateDTO;
    }

    /**
     * Gets the current game instance
     * 
     * @return The current Game object
     */
    public Game getCurrentGame() {
        if (currentGame == null) {
            throw new IllegalStateException("No game is currently active");
        }

        return currentGame;
    }

    public GameStateDTO drawCardSingleplayer(int playerId){
        if (playerId < 0 || playerId > 3 ) {
            throw new IllegalArgumentException("Player id must be between 0 and 3");
        }
        if (playerId != getSingleplayerGameState().getCurrentPlayerNo()){
            throw new IllegalArgumentException("Player id must be equal to current player id," + playerId + " " + getSingleplayerGameState().getCurrentPlayerNo());
        }

        Player player = currentGame.getPlayers().get(playerId);
        if (player.shouldDrawCard(currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor())){
            Card cardToDraw = currentGame.getDrawDeck().removeFirst();
            player.drawCard(cardToDraw);
            currentGame.refillDrawDeck();

            if (!CardUtil.isValidMove(cardToDraw, currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor())) {
                currentGame.setNextPlayerUnconditionally();
            }
        }

        return getSingleplayerGameState();
    }

    public GameStateDTO playComputerCard(int playerId) {
        if (playerId < 0 || playerId > 3 ) {
            throw new IllegalArgumentException("Player id must be between 0 and 3");
        }

        if (playerId != getSingleplayerGameState().getCurrentPlayerNo()){
            throw new IllegalArgumentException("Player id must be equal to current player id," + playerId + " " + getSingleplayerGameState().getCurrentPlayerNo());
        }

        ComputerPlayer player = (ComputerPlayer) currentGame.getPlayers().get(playerId);

        if (!player.shouldDrawCard(currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor())) {
            Card playedCard = player.playCard(currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor());

            currentGame.setLastPlayedCard(playedCard);

            currentGame.getDiscardDeck().add(playedCard);

            if (playedCard.isActionCard() && playedCard.getCardType() == CardType.REVERSE) {
                currentGame.setDirection(currentGame.getDirection().reverse());
            }

            currentGame.setNextPlayerConditionally();

            // Drawing +2 or +4 cards
            if (playedCard.getCardType() == CardType.DRAW_TWO) {
                currentGame.setDrawTwoStackAmount(currentGame.getDrawTwoStackAmount() + 1);
                int nextPlayerIndex = currentGame.getCurrentPlayer(); //already moved to next player
                Player nextPlayer = currentGame.getPlayers().get(nextPlayerIndex);
                boolean hasDrawTwo = nextPlayer.getDeck().stream()
                        .anyMatch(card -> card.getCardType() == CardType.DRAW_TWO);
                if(!hasDrawTwo){
                    for (int i = 0; i < 2 * currentGame.getDrawTwoStackAmount(); i++) {
                        Card cardToDraw = currentGame.getDrawDeck().removeFirst();
                        nextPlayer.drawCard(cardToDraw);
                    }
                    log.info("Player {} drew {} cards because of DRAW_TWO stacking", 
                            nextPlayerIndex, currentGame.getDrawTwoStackAmount() * 2);
                    currentGame.setDrawTwoStackAmount(0);//reset drawTwo stack
                    currentGame.setNextPlayerUnconditionally();//if next player draws cards, skip that player's turn
                }
            }

            else if (playedCard.getCardType() == CardType.WILD_DRAW_FOUR) {
                int nextPlayerIndex = currentGame.getCurrentPlayer();
                Player nextPlayer = currentGame.getPlayers().get(nextPlayerIndex);

                for (int i = 0; i < 4; i++) {
                    Card cardToDraw = currentGame.getDrawDeck().removeFirst();
                    nextPlayer.drawCard(cardToDraw);
                }

                currentGame.setNextPlayerUnconditionally();
            }

            // set next card color
            if (playedCard.isWildCard()) {
                CardColor mostFrequentColor = getMostFrequentCardColor(player);
                currentGame.setCurrentCardColor(mostFrequentColor);
            }
            else {
                currentGame.setCurrentCardColor(playedCard.getCardColor());
            }
        }

        else {
            return drawCardSingleplayer(playerId);
        }

        return getSingleplayerGameState();
    }

    private static CardColor getMostFrequentCardColor(Player player) {
        int maxCount = 0;
        CardColor mostFrequentColor = CardColor.RED;

        for (CardColor cardColor : CardColor.getColors()) {
            int count = 0;
            for (Card card : player.getDeck()) {
                if (card.getCardColor() == cardColor) {
                    if (++count > maxCount) {
                        maxCount = count;
                        mostFrequentColor = cardColor;
                    }
                }
            }
        }

        return mostFrequentColor;
    }

    public GameStateDTO playHumanCard(int playerId, int cardIndex, CardColor cardColor) {
        if (playerId < 0 || playerId > 3 ) {
            throw new IllegalArgumentException("Player id must be between 0 and 3");
        }

        if (playerId != getSingleplayerGameState().getCurrentPlayerNo()){
            throw new IllegalArgumentException("Player id must be equal to current player id," + playerId + " " + getSingleplayerGameState().getCurrentPlayerNo());
        }

        HumanPlayer player = (HumanPlayer) currentGame.getPlayers().get(playerId);

        if (cardIndex < 0 || cardIndex >= player.getDeck().size()) {
            throw new IllegalArgumentException("Invalid card index: " + cardIndex);
        }

        if (!player.shouldDrawCard(currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor())) {
            Card cardToPlay = player.getDeck().get(cardIndex);
            if (!CardUtil.isValidMove(cardToPlay, currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor())) {
                return getSingleplayerGameState();
            }
            if (cardToPlay.getCardType() == CardType.WILD_DRAW_FOUR) {
                //get list of playable cards excluding wild draw four cards
                List<Card> wildFourExcludedValidCards = player.getDeck().stream()
                    .filter(card -> card.getCardType() != CardType.WILD_DRAW_FOUR)
                    .filter(card -> CardUtil.isValidMove(card, currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor()))
                    .toList();
                if (!wildFourExcludedValidCards.isEmpty()) { //player has another card to play
                    return getSingleplayerGameState();
                }
            }
            if (currentGame.getDrawTwoStackAmount() > 0) {
                if(cardToPlay.getCardType() != CardType.DRAW_TWO){
                    boolean hasDrawTwo = player.getDeck().stream()
                        .anyMatch(card -> card.getCardType() == CardType.DRAW_TWO);
                    if(hasDrawTwo){
                        //force to play +2, there already exists +2 stack
                        log.warn("Human player has DRAW_TWO card but picked another card when there is a DRAW_TWO stack");
                        return getSingleplayerGameState();
                    }
                }
            }

            Card playedCard = player.playCard(cardIndex, currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor());

            currentGame.setLastPlayedCard(playedCard);

            currentGame.getDiscardDeck().add(playedCard);

            if (playedCard.isActionCard() && playedCard.getCardType() == CardType.REVERSE) {
                currentGame.setDirection(currentGame.getDirection().reverse());
            }

            currentGame.setNextPlayerConditionally(); //moving to next player

            // Drawing +2 or +4 cards
            if (playedCard.getCardType() == CardType.DRAW_TWO) {
                currentGame.setDrawTwoStackAmount(currentGame.getDrawTwoStackAmount() + 1);
                int nextPlayerIndex = currentGame.getCurrentPlayer();
                Player nextPlayer = currentGame.getPlayers().get(nextPlayerIndex);
                boolean hasDrawTwo = nextPlayer.getDeck().stream()
                        .anyMatch(card -> card.getCardType() == CardType.DRAW_TWO);
                if(!hasDrawTwo){
                    for (int i = 0; i < 2 * currentGame.getDrawTwoStackAmount(); i++) {
                        Card cardToDraw = currentGame.getDrawDeck().removeFirst();
                        nextPlayer.drawCard(cardToDraw);
                    }
                    log.info("Player {} drew {} cards because of DRAW_TWO stacking", 
                            nextPlayerIndex, currentGame.getDrawTwoStackAmount() * 2);
                    currentGame.setDrawTwoStackAmount(0);//reset drawTwo stack
                    currentGame.setNextPlayerUnconditionally();//if next player draws cards, skip that player's turn
                }
            }

            else if (playedCard.getCardType() == CardType.WILD_DRAW_FOUR) {
                int nextPlayerIndex = currentGame.getCurrentPlayer();
                Player nextPlayer = currentGame.getPlayers().get(nextPlayerIndex);

                for (int i = 0; i < 4; i++) {
                    Card cardToDraw = currentGame.getDrawDeck().removeFirst();
                    nextPlayer.drawCard(cardToDraw);
                }

                currentGame.setNextPlayerUnconditionally();
            }

            if (playedCard.isWildCard()) {
                log.info("Wild card played with picked color: {}", cardColor);
                currentGame.setCurrentCardColor(cardColor);
            }
            else {
                currentGame.setCurrentCardColor(playedCard.getCardColor());
            }
        }

        return getSingleplayerGameState();
    }

    public GameStateDTO useCheatCard(int playerId, String cheatType) {
        if (playerId!=0) {
            throw new IllegalArgumentException("Cheat card can only be played by the human player!");
        }

        if (currentGame.getCurrentPlayer() != 0) {
            return getSingleplayerGameState();
        }

        //set random color for the current card color
        CardColor randomColor = CardColor.values()[new Random().nextInt(4)];
        currentGame.setCurrentCardColor(randomColor);
        if (cheatType.equals("SKIP")) {
            currentGame.setLastPlayedCard(new ActionCard(CardType.SKIP, randomColor));
            currentGame.setNextPlayerConditionally();
        }
        else if (cheatType.equals("REVERSE")) {
            currentGame.setDirection(currentGame.getDirection().reverse());
            currentGame.setLastPlayedCard(new ActionCard(CardType.REVERSE, randomColor));
            currentGame.setNextPlayerUnconditionally();
        }
        else if (cheatType.equals("DRAW_TWO")) {
            currentGame.setLastPlayedCard(new ActionCard(CardType.DRAW_TWO, randomColor));
            currentGame.setDrawTwoStackAmount(currentGame.getDrawTwoStackAmount() + 1);
            currentGame.setNextPlayerUnconditionally();
            int nextPlayerIndex = currentGame.getCurrentPlayer();
            Player nextPlayer = currentGame.getPlayers().get(nextPlayerIndex);
            boolean hasDrawTwo = nextPlayer.getDeck().stream()
                    .anyMatch(card -> card.getCardType() == CardType.DRAW_TWO);
            if (!hasDrawTwo) {
                for (int i = 0; i < 2 * currentGame.getDrawTwoStackAmount(); i++) {
                    Card cardToDraw = currentGame.getDrawDeck().removeFirst();
                    nextPlayer.drawCard(cardToDraw);
                }
                log.info("Player {} drew {} cards because of DRAW_TWO stacking (cheat card)", 
                        nextPlayerIndex, currentGame.getDrawTwoStackAmount() * 2);
                currentGame.setDrawTwoStackAmount(0);//reset drawTwo stack
                currentGame.setNextPlayerUnconditionally(); // Skip the player who had to draw cards
            }
        }
        else if (cheatType.equals("WILD")) {
            //set a random color for the current card color
            currentGame.setLastPlayedCard(new WildCard(CardType.WILD));
            currentGame.setNextPlayerUnconditionally();
        }
        else if(cheatType.equals("WILD_SKIP_EVERYONE_ONCE")) {
            currentGame.setLastPlayedCard(new WildCard(CardType.WILD_SKIP_EVERYONE_ONCE));
        }
        else if (cheatType.equals("WILD_DRAW_FOUR")) {
            currentGame.setLastPlayedCard(new WildCard(CardType.WILD_DRAW_FOUR));
            currentGame.setNextPlayerUnconditionally();
            int nextPlayerIndex = currentGame.getCurrentPlayer();
            Player nextPlayer = currentGame.getPlayers().get(nextPlayerIndex);
            for (int i = 0; i < 4; i++) {
                Card cardToDraw = currentGame.getDrawDeck().removeFirst();
                nextPlayer.drawCard(cardToDraw);
            }
            currentGame.setNextPlayerUnconditionally();
        }
        else{
            throw new IllegalArgumentException("Invalid cheat type!");
        }
        return getSingleplayerGameState();
    }

    /**
     * Validates if a Wild Draw Four card can be played according to UNO rules.
     * A Wild Draw Four can only be played if the player has no other cards of the current color.
     * 
     * Note: This method is used internally for validation and is also used by the GameStateDTO
     * to provide this information to the frontend without requiring additional API calls.
     * 
     * @param playerId The ID of the player attempting to play the card
     * @param cardIndex The index of the Wild Draw Four card in the player's hand
     * @return true if the Wild Draw Four can be played, false otherwise
     */
    public boolean canPlayWildDrawFour(int playerId, int cardIndex) {
        if (playerId < 0 || playerId > 3) {
            throw new IllegalArgumentException("Player id must be between 0 and 3");
        }

        Player player = currentGame.getPlayers().get(playerId);

        if (cardIndex < 0 || cardIndex >= player.getDeck().size()) {
            throw new IllegalArgumentException("Invalid card index: " + cardIndex);
        }

        Card cardToPlay = player.getDeck().get(cardIndex);

        // Verify this is actually a Wild Draw Four card
        if (cardToPlay.getCardType() != CardType.WILD_DRAW_FOUR) {
            throw new IllegalArgumentException("Card at index " + cardIndex + " is not a Wild Draw Four card");
        }

        // Get list of playable cards excluding wild draw four cards
        List<Card> wildFourExcludedValidCards = player.getDeck().stream()
            .filter(card -> card.getCardType() != CardType.WILD_DRAW_FOUR)
            .filter(card -> CardUtil.isValidMove(card, currentGame.getLastPlayedCard(), currentGame.getCurrentCardColor()))
            .toList();

        // If player has no other playable cards, they can play Wild Draw Four
        return wildFourExcludedValidCards.isEmpty();
    }
}
