package tr.edu.metu.ceng.uno.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private String type;
    private String color;
    private Integer number;

    @Override
    public String toString() {
        return type + " " + color + " " + number;
    }
}