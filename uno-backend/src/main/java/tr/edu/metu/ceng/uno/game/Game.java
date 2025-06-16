package tr.edu.metu.ceng.uno.game;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tr.edu.metu.ceng.uno.card.Card;
import tr.edu.metu.ceng.uno.card.CardColor;
import tr.edu.metu.ceng.uno.card.CardType;
import tr.edu.metu.ceng.uno.player.Player;
import tr.edu.metu.ceng.uno.util.CardUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class Game {
    private volatile Direction direction = Direction.CLOCKWISE;
    private final List<Player> players;
    private final List<Card> drawDeck = Collections.synchronizedList(CardUtil.getShuffledDeck());
    private final List<Card> discardDeck = Collections.synchronizedList(new ArrayList<>());

    private volatile int currentPlayer = 0;
    private volatile CardColor currentCardColor;
    private volatile Card lastPlayedCardBeforeWildDrawFour;
    private volatile Card lastPlayedCard;
    private volatile int drawTwoStackAmount = 0;
    private volatile boolean canChallenge = false;

    public Game(List<Player> players) {
        log.debug("Creating new game instance with {} players", players.size());
        int size = players.size();
        if (size < 2 || size > 4) {
            log.error("Invalid player count: {}. Player count must be between 2 and 4!", size);
            throw new IllegalArgumentException("Player count must be between 2 and 4!");
        }
        this.players = new CopyOnWriteArrayList<>(players);
        log.trace("Player list copied to CopyOnWriteArrayList for thread safety");

        currentPlayer = 0; //TODO: randomizing later can be done
        log.debug("Initial current player index set to 0");

        drawTwoStackAmount = 0;
        log.debug("Initial drawTwoStackAmount set to 0");

        log.info("Game created with {} players: {}", size, 
                players.stream().map(Player::getUsername).collect(Collectors.joining(", ")));
        log.debug("Game initial direction: {}", direction);
    }

    public synchronized void initializeFirstCard(){
        log.info("Initializing first card for the game");
        log.debug("Draw deck size before initialization: {}", drawDeck.size());

        int attempts = 0;
        do {
            attempts++;
            log.trace("Attempt #{} to find valid first card", attempts);
            lastPlayedCard = drawDeck.removeFirst();
            discardDeck.add(lastPlayedCard);
            log.debug("Drew card: {} {}. Checking if valid as first card", 
                    lastPlayedCard.getCardColor(), lastPlayedCard.getCardType());

            if (lastPlayedCard.getCardType() == CardType.WILD_DRAW_FOUR) {
                log.debug("Card {} {} is not valid as first card, trying again", 
                        lastPlayedCard.getCardColor(), lastPlayedCard.getCardType());
            }
        } while (lastPlayedCard.getCardType() == CardType.WILD_DRAW_FOUR);

        log.info("First card initialized: {} {}", lastPlayedCard.getCardColor(), lastPlayedCard.getCardType());
        log.debug("Draw deck size after initialization: {}", drawDeck.size());
        log.debug("Discard deck size after initialization: {}", discardDeck.size());

        setNextColor();
        log.debug("Current color set to: {}", currentCardColor);

        Player firstPlayer = players.get(currentPlayer);
        log.debug("First player is {} (index: {})", firstPlayer.getUsername(), currentPlayer);

        if (lastPlayedCard.getCardType() == CardType.SKIP) {
            log.info("First card is SKIP. Skipping first player's turn");
            log.debug("Player {} will be skipped", firstPlayer.getUsername());
            setNextPlayerUnconditionally();
            log.debug("Turn moved to player {} (index: {})", 
                    players.get(currentPlayer).getUsername(), currentPlayer);
        }
        else if (lastPlayedCard.getCardType() == CardType.REVERSE) {
            log.info("First card is REVERSE. Reversing game direction to {}", direction.reverse());
            Direction oldDirection = direction;
            direction = direction.reverse();
            log.debug("Game direction changed from {} to {}", oldDirection, direction);
        }
        else if (lastPlayedCard.getCardType() == CardType.DRAW_TWO) {
            log.info("First card is DRAW_TWO");
            log.debug("Checking if player {} has a DRAW_TWO card", firstPlayer.getUsername());

            if (players.get(currentPlayer).hasDrawTwoCard()) {
                log.info("Player {} has a DRAW_TWO card. Setting drawTwoStackAmount to 1", 
                        players.get(currentPlayer).getUsername());
                drawTwoStackAmount = 1;
                log.debug("drawTwoStackAmount set to {}", drawTwoStackAmount);
            }
            else {
                log.info("Player {} doesn't have a DRAW_TWO card. Drawing 2 cards", 
                        players.get(currentPlayer).getUsername());
                drawTwoStackAmount = 0;
                log.debug("drawTwoStackAmount reset to {}", drawTwoStackAmount);

                log.debug("Drawing 2 cards for player {}", firstPlayer.getUsername());
                for (int i = 0; i < 2; i++) {
                    Card drawnCard = drawDeck.removeFirst();
                    players.get(currentPlayer).drawCard(drawnCard);
                    log.debug("Player {} drew card: {} {}", 
                            players.get(currentPlayer).getUsername(), 
                            drawnCard.getCardColor(), drawnCard.getCardType());
                }
                log.debug("Player {} now has {} cards", 
                        firstPlayer.getUsername(), firstPlayer.getDeck().size());

                log.debug("Moving to next player after DRAW_TWO effect");
                setNextPlayerUnconditionally();
                log.debug("Turn moved to player {} (index: {})", 
                        players.get(currentPlayer).getUsername(), currentPlayer);
            }
        } else {
            log.debug("First card is a regular card. No special action needed.");
        }

        log.debug("First card initialization completed");
    }

    public synchronized boolean anyPlayerHasEmptyDeck(){
        log.debug("Checking if any player has an empty deck");

        boolean gameEnded = players.stream().anyMatch(player -> player.getDeck().isEmpty());
        log.debug("Game ended check result: {}", gameEnded);

        if (gameEnded) {
            log.debug("Game has ended. Determining winner...");

            // Log each player's remaining card count
            players.forEach(player -> 
                log.debug("Player {} has {} cards remaining", 
                        player.getUsername(), player.getDeck().size())
            );

            Player winner = players.stream()
                .filter(player -> player.getDeck().isEmpty())
                .findFirst()
                .orElse(null);

            if (winner != null) {
                log.info("Game ended! Player {} has won the game by emptying their deck", winner.getUsername());

                // Log other players' remaining cards for debugging
                players.stream()
                    .filter(player -> !player.equals(winner))
                    .forEach(player -> {
                        log.debug("Losing player {} has {} cards remaining: {}", 
                                player.getUsername(), 
                                player.getDeck().size(),
                                player.getDeck().stream()
                                    .map(card -> card.getCardColor() + " " + card.getCardType())
                                    .collect(Collectors.joining(", ")));
                    });
            } else {
                log.warn("Game ended but couldn't determine the winner");
                log.debug("This is an unexpected state and might indicate a bug in the game logic");
            }
        } else {
            log.trace("Game continues - no player has an empty deck yet");
        }

        return gameEnded;
    }

    public synchronized void setNextColor() {
        log.debug("Setting next color based on last played card: {} {}", 
                lastPlayedCard.getCardColor(), lastPlayedCard.getCardType());

        CardColor previousColor = currentCardColor;
        log.trace("Previous color was: {}", previousColor);

        currentCardColor = lastPlayedCard.getCardColor();
        log.debug("Initial color from card: {}", currentCardColor);

        if (currentCardColor == CardColor.NONE) {
            log.debug("Card has NONE color (likely a wild card). Selecting a random color");

            // Get available colors (excluding NONE)
            CardColor[] availableColors = CardColor.values();
            log.trace("Available colors: {}", String.join(", ", 
                    java.util.Arrays.stream(availableColors)
                    .map(CardColor::name)
                    .collect(Collectors.toList())));

            // Select random color
            CardColor randomColor = CardColor.values()[new Random().nextInt(4)];
            log.info("Wild card played. Randomly selecting color: {}", randomColor);

            currentCardColor = randomColor;
            log.debug("Color changed from NONE to {}", currentCardColor);
        } else {
            log.info("Color set to: {}", currentCardColor);

            if (previousColor != null && previousColor != currentCardColor) {
                log.debug("Color changed from {} to {}", previousColor, currentCardColor);
            } else if (previousColor == currentCardColor) {
                log.debug("Color remained the same: {}", currentCardColor);
            }
        }

        log.trace("setNextColor completed. Current color is now: {}", currentCardColor);
    }

    public synchronized void distributeCards() {
        log.info("Distributing initial cards to {} players", players.size());
        log.debug("Draw deck size before distribution: {}", drawDeck.size());

        int totalCardsToDistribute = players.size() * 7;
        log.debug("Total cards to distribute: {} (7 cards × {} players)", totalCardsToDistribute, players.size());

        if (drawDeck.size() < totalCardsToDistribute) {
            log.warn("Draw deck has only {} cards, but {} cards are needed for distribution", 
                    drawDeck.size(), totalCardsToDistribute);
        }

        players.forEach(player -> {
            log.debug("Distributing cards to player: {}", player.getUsername());
            List<String> cardDescriptions = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                Card card = drawDeck.removeFirst();
                player.getDeck().add(card);

                String cardDesc = card.getCardColor() + " " + card.getCardType();
                cardDescriptions.add(cardDesc);

                log.trace("Player {} received card {}/{}: {} {}", 
                        player.getUsername(), (i+1), 7, 
                        card.getCardColor(), card.getCardType());
            }

            log.debug("Player {} received 7 cards: {}", 
                    player.getUsername(), String.join(", ", cardDescriptions));
            log.trace("Player {} now has {} cards in total", player.getUsername(), player.getDeck().size());
        });

        log.info("Card distribution completed. Draw deck has {} cards remaining", drawDeck.size());
        log.debug("Each player now has 7 cards in their deck");

        // Log a summary of each player's initial hand
        if (log.isDebugEnabled()) {
            StringBuilder summary = new StringBuilder("Initial hands summary:\n");
            for (Player player : players) {
                summary.append(String.format("- %s: %d cards\n", player.getUsername(), player.getDeck().size()));
            }
            log.debug(summary.toString());
        }
    }

    /**
     * Sets the game's current player to the next player based on the game direction
     * Alias for setNextPlayerUnconditionally for consistency with new method names
     */
    public synchronized void setNextPlayer() {
        log.debug("Setting next player (alias for setNextPlayerUnconditionally)");
        setNextPlayerUnconditionally();
    }

    /**
     * Gets the next player in the game sequence without changing the current player
     * @return The next player
     */
    public synchronized Player getNextPlayer() {
        int playerCount = players.size();
        int nextIndex;

        if (direction == Direction.CLOCKWISE) {
            nextIndex = (currentPlayer + 1) % playerCount;
        } else {
            nextIndex = (currentPlayer - 1 + playerCount) % playerCount;
        }

        Player nextPlayer = players.get(nextIndex);
        log.debug("Next player would be: {} (index: {})", nextPlayer.getUsername(), nextIndex);
        return nextPlayer;
    }

    public synchronized void setNextPlayerUnconditionally() {
        log.debug("Setting next player unconditionally");

        int playerCount = players.size();
        log.trace("Total players in game: {}", playerCount);

        int index = currentPlayer;
        log.trace("Current player index: {}", index);

        int nextIndex = -1;

        if (direction == Direction.CLOCKWISE) {
            log.trace("Game direction is CLOCKWISE, moving to next player");
            nextIndex = (index + 1) % playerCount;
            log.debug("Next player calculation: ({} + 1) % {} = {}", index, playerCount, nextIndex);
        }
        else if (direction == Direction.COUNTER_CLOCKWISE) {
            log.trace("Game direction is COUNTER_CLOCKWISE, moving to previous player");
            nextIndex = (index - 1 + playerCount) % playerCount;
            log.debug("Next player calculation: ({} - 1 + {}) % {} = {}", 
                    index, playerCount, playerCount, nextIndex);
        }

        Player currentPlayerObj = players.get(currentPlayer);
        Player nextPlayerObj = players.get(nextIndex);

        log.debug("Current player: {} (index: {}) has {} cards", 
                currentPlayerObj.getUsername(), currentPlayer, currentPlayerObj.getDeck().size());
        log.debug("Next player: {} (index: {}) has {} cards", 
                nextPlayerObj.getUsername(), nextIndex, nextPlayerObj.getDeck().size());

        log.info("Turn changing from player {} (index: {}) to player {} (index: {})", 
                currentPlayerObj.getUsername(), currentPlayer, 
                nextPlayerObj.getUsername(), nextIndex);

        int oldIndex = currentPlayer;
        currentPlayer = nextIndex;
        log.trace("Player index updated from {} to {}", oldIndex, currentPlayer);

        log.debug("Turn successfully changed to player {}", nextPlayerObj.getUsername());
    }

    public synchronized void setNextPlayerConditionally() {
        log.debug("Setting next player conditionally based on last played card: {} {}", 
                lastPlayedCard.getCardColor(), lastPlayedCard.getCardType());
        log.trace("Card type: {}, Card color: {}", lastPlayedCard.getCardType(), lastPlayedCard.getCardColor());

        int playerCount = players.size();
        log.trace("Total players in game: {}", playerCount);

        if(lastPlayedCard.getCardType() == CardType.WILD_SKIP_EVERYONE_ONCE){
            //same player should play again
            return;
        }

        int index = currentPlayer;
        log.trace("Current player index before conditional logic: {}", index);

        int nextIndex = -1;
        Player skippedPlayer = null;

        if (direction == Direction.CLOCKWISE) {
            log.debug("Game direction is CLOCKWISE");

            if (lastPlayedCard.getCardType() == CardType.SKIP) {
                log.info("SKIP card effect: Skipping next player in CLOCKWISE direction");

                // Calculate who would be skipped
                int skippedIndex = (index + 1) % playerCount;
                skippedPlayer = players.get(skippedIndex);
                log.debug("Player {} (index: {}) will be skipped", 
                        skippedPlayer.getUsername(), skippedIndex);

                // Skip by incrementing index
                index++;
                log.debug("Index incremented to {} to skip next player", index);
            } else {
                log.debug("No SKIP effect, proceeding to next player normally");
            }

            // Calculate next player
            nextIndex = (index + 1) % playerCount;
            log.debug("Next player calculation: ({} + 1) % {} = {}", index, playerCount, nextIndex);
        }
        else if (direction == Direction.COUNTER_CLOCKWISE) {
            log.debug("Game direction is COUNTER_CLOCKWISE");

            if (lastPlayedCard.getCardType() == CardType.SKIP) {
                log.info("SKIP card effect: Skipping next player in COUNTER_CLOCKWISE direction");

                // Calculate who would be skipped
                int skippedIndex = (index - 1 + playerCount) % playerCount;
                skippedPlayer = players.get(skippedIndex);
                log.debug("Player {} (index: {}) will be skipped", 
                        skippedPlayer.getUsername(), skippedIndex);

                // Skip by decrementing index
                index--;
                log.debug("Index decremented to {} to skip previous player", index);
            } else {
                log.debug("No SKIP effect, proceeding to previous player normally");
            }

            // Calculate next player
            nextIndex = (index - 1 + playerCount) % playerCount;
            log.debug("Next player calculation: ({} - 1 + {}) % {} = {}", 
                    index, playerCount, playerCount, nextIndex);
        }

        Player currentPlayerObj = players.get(currentPlayer);
        Player nextPlayerObj = players.get(nextIndex);

        log.debug("Current player: {} (index: {}) has {} cards", 
                currentPlayerObj.getUsername(), currentPlayer, currentPlayerObj.getDeck().size());
        log.debug("Next player: {} (index: {}) has {} cards", 
                nextPlayerObj.getUsername(), nextIndex, nextPlayerObj.getDeck().size());

        if (skippedPlayer != null) {
            log.debug("Skipped player: {} (has {} cards)", 
                    skippedPlayer.getUsername(), skippedPlayer.getDeck().size());
        }

        log.info("Turn changing conditionally from player {} (index: {}) to player {} (index: {})", 
                currentPlayerObj.getUsername(), currentPlayer, 
                nextPlayerObj.getUsername(), nextIndex);

        int oldIndex = currentPlayer;
        currentPlayer = nextIndex;
        log.trace("Player index updated from {} to {}", oldIndex, currentPlayer);

        log.debug("Turn successfully changed to player {}", nextPlayerObj.getUsername());
    }

    public synchronized void refillDrawDeck() {
        log.debug("Checking if draw deck needs refilling. Current size: {}", drawDeck.size());

        if (drawDeck.isEmpty()) {
            log.info("Draw deck is empty. Refilling from discard deck with {} cards", discardDeck.size());

            if (discardDeck.isEmpty()) {
                log.warn("Both draw deck and discard deck are empty! This is an unexpected state.");
                return;
            }

            log.debug("Shuffling discard deck before refilling draw deck");
            Collections.shuffle(discardDeck);
            log.trace("Discard deck shuffled");

            // Save the last played card before moving all cards
            Card lastCard = null;
            if (!discardDeck.isEmpty()) {
                lastCard = discardDeck.get(discardDeck.size() - 1);
                log.debug("Preserving last played card: {} {}", 
                        lastCard.getCardColor(), lastCard.getCardType());
            }

            // Move all cards except the last played card
            List<Card> cardsToMove = new ArrayList<>(discardDeck);
            if (lastCard != null) {
                cardsToMove.remove(lastCard);
                log.trace("Removed last played card from cards to move");
            }

            log.debug("Moving {} cards from discard deck to draw deck", cardsToMove.size());
            drawDeck.addAll(cardsToMove);
            log.debug("Draw deck refilled with {} cards", drawDeck.size());

            // Clear discard deck and add back the last played card
            discardDeck.clear();
            log.debug("Discard deck cleared");

            if (lastCard != null) {
                discardDeck.add(lastCard);
                log.debug("Last played card added back to discard deck");
            }

            log.info("Draw deck successfully refilled. New size: {}, Discard deck size: {}", 
                    drawDeck.size(), discardDeck.size());
        } else {
            log.trace("Draw deck has {} cards, no need to refill", drawDeck.size());
        }
    }
}
