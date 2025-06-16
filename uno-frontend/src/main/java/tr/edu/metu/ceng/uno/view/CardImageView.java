package tr.edu.metu.ceng.uno.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import tr.edu.metu.ceng.uno.model.CardDTO;

import java.io.InputStream;

@Slf4j
public class CardImageView extends ImageView {
    private int playerId;
    private CardDTO card;

    public CardImageView(int playerId) {
        super();
        this.playerId = playerId;
    }

    public CardImageView(Image image, int playerId) {
        super(image);
        this.playerId = playerId;
    }

    public CardImageView(Image image) {
        super(image);
        this.playerId = 0; // Default to human player
    }

    public CardImageView(CardDTO card) {
        super();
        this.card = card;
        this.playerId = 0; // Default to human player

        // Load the appropriate image based on the card
        String imagePath = getImagePathForCard(card);
        InputStream imageStream = getClass().getResourceAsStream(imagePath);

        if (imageStream == null) {
            log.error("Failed to load image at path: {}", imagePath);
            return;
        }

        Image cardImage = new Image(imageStream);
        setImage(cardImage);
        setPreserveRatio(true);
    }

    /**
     * Sets the card data for this CardImageView
     * 
     * @param card The card data to store
     */
    public void setCardData(CardDTO card) {
        this.card = card;
    }

    /**
     * Gets the image path for a card
     * 
     * @param card The card
     * @return The image path
     */
    private String getImagePathForCard(CardDTO card) {
        String cardType = card.getType();

        if (cardType.equals("WILD")) {
            return "/cards/WILD_CARD_CHANGE_COLOR.png";
        } else if (cardType.equals("WILD_DRAW_FOUR")) {
            return "/cards/WILD_CARD_DRAW_4.png";
        } else if (cardType.equals("WILD_SKIP_EVERYONE_ONCE")) {
            return "/cards/WILD_SKIP_EVERYONE_ONCE.png";
        } else {
            String cardColor = card.getColor();
            if (cardType.equals("NUMBER")) {
                return String.format("/cards/%s_%d.png", cardColor, card.getNumber());
            } else if (cardType.equals("SKIP")) {
                return String.format("/cards/%s_SKIP.png", cardColor);
            } else if (cardType.equals("REVERSE")) {
                return String.format("/cards/%s_REVERSE.png", cardColor);
            } else if (cardType.equals("DRAW_TWO")) {
                return String.format("/cards/%s_DRAW_2.png", cardColor);
            } else {
                log.warn("Unknown card type: {}", card);
                return "/cards/WILD_CARD_EMPTY.png"; // placeholder
            }
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public CardDTO getCard() {
        return card;
    }
}
