package tr.edu.metu.ceng.uno.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameStateDTO {
    private List<PlayerDTO> players;
    private int currentPlayerNo;
    private CardDTO lastPlayedCard;
    private String currentColor;
    private String direction;
    private int drawDeckSize;
    private boolean gameFinished;
    private String winner;
    private boolean canPlayWildDrawFour;
    private String quittingPlayerName;
    private boolean canChallenge; //other player played draw 4
}