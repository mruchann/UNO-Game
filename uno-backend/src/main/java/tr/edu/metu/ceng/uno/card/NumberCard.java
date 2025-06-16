package tr.edu.metu.ceng.uno.card;

public class NumberCard extends Card {
    private final int number;

    public NumberCard(CardColor cardColor, int number) {
        super(CardType.NUMBER, cardColor);
        this.number = number;
    }

    public int getCardNumber() {
        return number;
    }

    @Override
    public String toString() {
        return getCardColor() + " " + number;
    }
}
