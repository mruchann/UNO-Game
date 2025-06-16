package tr.edu.metu.ceng.uno.util;

import lombok.extern.slf4j.Slf4j;
import tr.edu.metu.ceng.uno.card.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CardUtil {
    public static List<Card> getShuffledDeck() {
        log.info("Creating new shuffled deck");
        List<Card> deck = new LinkedList<>();
        log.debug("Initialized empty deck");

        int numberCards = 0;
        int actionCards = 0;
        int wildCards = 0;
        int wildCustomizables = 0;

        log.debug("Creating deck with 2 sets of cards");
        for (int i = 0; i < 2; i++) {
            log.trace("Creating set {} of cards", i+1);

            // Add number cards (0-9) of each color
            log.debug("Adding number cards (0-9) for each color");
            for (int number = 0; number < 10; number++) {
                for (CardColor color : CardColor.values()) {
                    if (color == CardColor.NONE) {
                        log.trace("Skipping NONE color for number card {}", number);
                        continue;
                    }
                    deck.add(new NumberCard(color, number));
                    numberCards++;
                    log.trace("Added number card: {} {}", color, number);
                }
            }
            log.debug("Added {} number cards in set {}", numberCards/2, i+1);

            // Add action cards of each color
            log.debug("Adding action cards for each color");
            for (CardColor color : CardColor.values()) {
                if (color == CardColor.NONE) {
                    log.trace("Skipping NONE color for action cards");
                    continue;
                }
                deck.add(new ActionCard(CardType.SKIP, color));
                actionCards++;
                log.trace("Added SKIP card: {}", color);

                deck.add(new ActionCard(CardType.REVERSE, color));
                actionCards++;
                log.trace("Added REVERSE card: {}", color);

                deck.add(new ActionCard(CardType.DRAW_TWO, color));
                actionCards++;
                log.trace("Added DRAW_TWO card: {}", color);
            }
            log.debug("Added {} action cards in set {}", actionCards/2, i+1);

            // Add wild cards
            log.debug("Adding wild cards");
            for (int j = 0; j < 4; j++) {
                deck.add(new WildCard(CardType.WILD));
                wildCards++;
                log.trace("Added WILD card #{}", j+1);

                deck.add(new WildCard(CardType.WILD_DRAW_FOUR));
                wildCards++;
                log.trace("Added WILD_DRAW_FOUR card #{}", j+1);
            }
            log.debug("Added {} wild cards in set {}", wildCards/2, i+1);
        }

        for(int i = 0; i < 3; i ++){
            log.trace("Adding skip everyone once wild card");
            deck.add(new WildCard(CardType.WILD_SKIP_EVERYONE_ONCE));
            wildCustomizables++;
        }

        log.info("Deck created with {} cards: {} number cards, {} action cards, {} wild cards, {} wild customizables",
                deck.size(), numberCards, actionCards, wildCards, wildCustomizables);

        log.debug("Shuffling deck");
        Collections.shuffle(deck);
        log.debug("Deck shuffled");

        return deck;
    }

    public static boolean isValidMove(Card cardToPlay, Card lastPlayedCard, CardColor currentCardColor) {
        log.debug("Checking if card {} {} can be played on {} {} with current color {}", 
                cardToPlay.getCardColor(), cardToPlay.getCardType(),
                lastPlayedCard.getCardColor(), lastPlayedCard.getCardType(),
                currentCardColor);

        // WildCard Card
        if (cardToPlay.isWildCard()) {
            log.debug("Card {} {} is a wild card, which can always be played", 
                    cardToPlay.getCardColor(), cardToPlay.getCardType());
            return true;
        }

        // Action and Number Cards - check if colors match
        if (cardToPlay.getCardColor() == currentCardColor) {
            log.debug("Card {} {} matches the current color {}, valid move", 
                    cardToPlay.getCardColor(), cardToPlay.getCardType(), currentCardColor);
            return true;
        }

        // Number and Action Cards - check if types match
        if (cardToPlay.getCardType() == lastPlayedCard.getCardType()) {
            log.debug("Card types match: {}", cardToPlay.getCardType());

            // For number cards, check if the numbers match
            if (cardToPlay.isNumberCard()) {
                NumberCard numberCardToPlay = (NumberCard) cardToPlay;
                NumberCard numberCardLastPlayed = (NumberCard) lastPlayedCard;

                boolean numbersMatch = numberCardToPlay.getCardNumber() == numberCardLastPlayed.getCardNumber();

                if (numbersMatch) {
                    log.debug("Number cards match: {} {} and {} {}", 
                            numberCardToPlay.getCardColor(), numberCardToPlay.getCardNumber(),
                            numberCardLastPlayed.getCardColor(), numberCardLastPlayed.getCardNumber());
                } else {
                    log.debug("Number cards don't match: {} {} and {} {}", 
                            numberCardToPlay.getCardColor(), numberCardToPlay.getCardNumber(),
                            numberCardLastPlayed.getCardColor(), numberCardLastPlayed.getCardNumber());
                }

                return numbersMatch;
            }

            // Action Card - if types match, it's a valid move
            log.debug("Action cards of same type can be played regardless of color: {}", 
                    cardToPlay.getCardType());
            return true;
        }

        log.debug("Card {} {} cannot be played on {} {} with current color {}", 
                cardToPlay.getCardColor(), cardToPlay.getCardType(),
                lastPlayedCard.getCardColor(), lastPlayedCard.getCardType(),
                currentCardColor);
        return false;
    }
}
