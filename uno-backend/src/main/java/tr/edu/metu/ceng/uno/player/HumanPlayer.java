package tr.edu.metu.ceng.uno.player;

import tr.edu.metu.ceng.uno.card.Card;
import tr.edu.metu.ceng.uno.card.CardColor;

public class HumanPlayer extends Player {

    public HumanPlayer(String username) {
        super(username);
    }

    public Card playCard(int cardIndex, Card lastPlayedCard, CardColor currentCardColor) {
        Card cardToPlay = deck.get(cardIndex);
        deck.remove(cardToPlay);
        return cardToPlay;
    }
}
