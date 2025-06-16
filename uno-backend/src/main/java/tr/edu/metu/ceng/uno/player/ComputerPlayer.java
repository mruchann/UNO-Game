package tr.edu.metu.ceng.uno.player;

import tr.edu.metu.ceng.uno.card.Card;
import tr.edu.metu.ceng.uno.card.CardColor;
import tr.edu.metu.ceng.uno.util.CardUtil;

import java.util.Comparator;

public class ComputerPlayer extends Player {

    public ComputerPlayer(String username) {
        super(username);
    }

    /***
     *  Finds the card that should be played for computer player and removes that card from deck
     * @param lastPlayedCard
     * @param currentCardColor
     * @return
     */

    public Card playCard(Card lastPlayedCard, CardColor currentCardColor) {
        /*
        * 1. Action
        * 2. Number
        * 3. Wild
        * */
        Card cardToPlay = deck.stream()
            .filter(card -> CardUtil.isValidMove(card, lastPlayedCard, currentCardColor))
            .min(Comparator.comparing(Card::getCardType)).get();

        deck.remove(cardToPlay);
        return cardToPlay;
    }
}
