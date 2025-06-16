package tr.edu.metu.ceng.uno.card;

public class WildCard extends Card {
    public WildCard(CardType cardType) {
        super(cardType, CardColor.NONE);
    }

    @Override
    public String toString() {
        return getCardType().toString();
    }
}
