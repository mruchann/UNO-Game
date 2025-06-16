package tr.edu.metu.ceng.uno.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tr.edu.metu.ceng.uno.card.Card;
import tr.edu.metu.ceng.uno.card.NumberCard;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private String type;
    private String color;
    private Integer number;

    public static CardDTO createCardDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setType(card.getCardType().toString());
        dto.setColor(card.getCardColor().toString());
        
        // For number cards, also setting the number value
        if (card.isNumberCard()) {
            NumberCard numberCard = (NumberCard) card;
            dto.setNumber(numberCard.getCardNumber());
        }
        
        return dto;
    }
}