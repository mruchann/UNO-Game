package tr.edu.metu.ceng.uno.player;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tr.edu.metu.ceng.uno.card.Card;
import tr.edu.metu.ceng.uno.card.CardType;
import tr.edu.metu.ceng.uno.card.NumberCard;
import tr.edu.metu.ceng.uno.card.CardColor;
import tr.edu.metu.ceng.uno.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public abstract class Player {
    protected final String username;
    protected final List<Card> deck = new ArrayList<>();

    public Player(String username) {
        this.username = username;
        log.info("Player created: {}", username);
    }

    /***
     *  Method to check whether the player's draw card action should be done
     * @param lastPlayedCard
     * @param currentCardColor
     * @return
     */
    public boolean shouldDrawCard(Card lastPlayedCard, CardColor currentCardColor) {
        log.debug("Checking if player {} should draw a card. Last played card: {} {}, Current color: {}", 
                username, lastPlayedCard.getCardColor(), lastPlayedCard.getCardType(), currentCardColor);

        for (Card card : deck) {
            if (CardUtil.isValidMove(card, lastPlayedCard, currentCardColor)) {
                log.debug("Player {} has a valid card to play: {} {}", 
                        username, card.getCardColor(), card.getCardType());
                return false;
            }
        }

        log.info("Player {} has no valid cards to play and must draw a card", username);
        return true;
    }

    public boolean hasDrawTwoCard() {
        boolean hasCard = deck.stream().anyMatch(card -> card.getCardType() == CardType.DRAW_TWO);
        log.debug("Checking if player {} has a DRAW_TWO card: {}", username, hasCard);
        return hasCard;
    }

    public void drawCard(Card cardToDraw) {
        deck.add(cardToDraw);
        log.info("Player {} drew a card: {} {}", 
                username, cardToDraw.getCardColor(), cardToDraw.getCardType());
        log.debug("Player {} now has {} cards", username, deck.size());
    }

    /**
     * Alias for drawCard for consistency with new method names
     * @param card Card to add to the player's hand
     */
    public void addCard(Card card) {
        deck.add(card);
        log.info("Player {} added a card: {} {}", 
                username, card.getCardColor(), card.getCardType());
        log.debug("Player {} now has {} cards", username, deck.size());
    }

    /**
     * Gets the player's current hand of cards
     * @return List of cards in the player's hand
     */
    public List<Card> getCards() {
        log.debug("Getting all cards for player {}. Card count: {}", username, deck.size());
        return deck;
    }

    /**
     * Checks if the player has a card of the specified type
     * @param cardType The card type to check for
     * @param drawDeck The draw deck (not used in this implementation but kept for compatibility)
     * @return true if the player has a card of the specified type
     */
    public boolean hasCardType(CardType cardType, List<Card> drawDeck) {
        boolean hasCard = deck.stream().anyMatch(card -> card.getCardType() == cardType);
        log.debug("Checking if player {} has a card of type {}: {}", username, cardType, hasCard);
        return hasCard;
    }

    /**
     * Gets all cards of a specific type from the player's hand
     * @param cardType The card type to retrieve
     * @return List of cards of the specified type
     */
    public List<Card> getCardsOfType(CardType cardType) {
        List<Card> cards = deck.stream()
                .filter(card -> card.getCardType() == cardType)
                .collect(Collectors.toList());
        log.debug("Player {} has {} cards of type {}", username, cards.size(), cardType);
        return cards;
    }

    public void stringifyDeck(){
        log.info("Displaying deck for player {}", username);

        // Note: System.out.println statements are kept for backward compatibility
        // with existing code that may rely on console output
        if (deck.isEmpty()) {
            log.info("Player {}'s deck is empty!", username);
            System.out.println("Player " + username + "'s deck:");
            System.out.println("==============================");
            System.out.println("Deck is empty!");
            System.out.println("==============================");
            return;
        }

        StringBuilder deckInfo = new StringBuilder();
        deckInfo.append("Player ").append(username).append("'s deck:\n");
        deckInfo.append("==============================\n");

        for (int i = 0; i < deck.size(); i++) {
            Card card = deck.get(i);
            String cardInfo;

            if (card.isNumberCard()) {
                NumberCard numberCard = (NumberCard) card;
                cardInfo = numberCard.getCardColor() + " " + numberCard.getCardNumber();
            } else if (card.isActionCard()) {
                cardInfo = card.getCardColor() + " " + card.getCardType();
            } else { // Wild card
                cardInfo = card.getCardType().toString();
            }

            deckInfo.append((i + 1)).append(". ").append(cardInfo).append("\n");
        }
        deckInfo.append("==============================");

        log.debug("Player {}'s deck contents: {}", username, deckInfo.toString());

        // Keep the original console output for backward compatibility
        // with existing code that may rely on console output
        System.out.println("Player " + username + "'s deck:");
        System.out.println("==============================");

        for (int i = 0; i < deck.size(); i++) {
            Card card = deck.get(i);
            String cardInfo;

            if (card.isNumberCard()) {
                NumberCard numberCard = (NumberCard) card;
                cardInfo = numberCard.getCardColor() + " " + numberCard.getCardNumber();
            } else if (card.isActionCard()) {
                cardInfo = card.getCardColor() + " " + card.getCardType();
            } else { // Wild card
                cardInfo = card.getCardType().toString();
            }

            System.out.println((i + 1) + ". " + cardInfo);
        }
        System.out.println("==============================");
    }
}
