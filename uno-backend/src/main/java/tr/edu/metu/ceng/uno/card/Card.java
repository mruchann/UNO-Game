package tr.edu.metu.ceng.uno.card;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Card {
    private final CardType cardType;
    private CardColor cardColor;

    public Card(CardType cardType, CardColor cardColor) {
        this.cardType = cardType;
        this.cardColor = cardColor;
        log.debug("Card created: {} {}", cardColor, cardType);
    }

    public CardType getCardType() {
        return cardType;
    }

    public CardColor getCardColor() {
        return cardColor;
    }

    public CardColor setCardColor(CardColor cardColor) {
        log.info("Card color changing from {} to {} for card type {}", this.cardColor, cardColor, this.cardType);
        return this.cardColor = cardColor;
    }

    public boolean isWildCard() {
        boolean isWild = cardType == CardType.WILD || cardType == CardType.WILD_DRAW_FOUR || cardType == CardType.WILD_SKIP_EVERYONE_ONCE;
        log.trace("Checking if card {} {} is wild card: {}", cardColor, cardType, isWild);
        return isWild;
    }

    public boolean isActionCard() {
        boolean isAction = cardType == CardType.SKIP || cardType == CardType.REVERSE || cardType == CardType.DRAW_TWO;
        log.trace("Checking if card {} {} is action card: {}", cardColor, cardType, isAction);
        return isAction;
    }

    public boolean isNumberCard() {
        boolean isNumber = cardType == CardType.NUMBER;
        log.trace("Checking if card {} {} is number card: {}", cardColor, cardType, isNumber);
        return isNumber;
    }
}
