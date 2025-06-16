package tr.edu.metu.ceng.uno.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tr.edu.metu.ceng.uno.player.Player;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private String username;
    private List<CardDTO> deck;

    public static PlayerDTO createPLayerDTOForCurrentPlayer(Player player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setUsername(player.getUsername());
        dto.setDeck(player.getDeck().stream()
                .map(CardDTO::createCardDTO)
                .collect(Collectors.toList()));
        return dto;
    }
}