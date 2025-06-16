package tr.edu.metu.ceng.uno.card;

public class ActionCard extends Card {
    public ActionCard(CardType cardType, CardColor cardColor) {
        super(cardType, cardColor);
    }

    @Override
    public String toString() {
        return getCardColor() + " " + getCardType();
    }
}
