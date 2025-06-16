package tr.edu.metu.ceng.uno.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiplayerMessage {
    
    public enum MessageType {
        ROOM_JOIN,
        ROOM_QUIT,
        GAME_START,
        DRAW_CARD,
        PLAY_CARD,
        GAME_END,
        CHALLENGE,
        REJECT_CHALLENGE
    }
    
    private MessageType type;
    private String username;
    private String gameId;
    private Integer playerIndex;
    private Integer cardIndex;
    private String cardColor;
    private String content;
} 