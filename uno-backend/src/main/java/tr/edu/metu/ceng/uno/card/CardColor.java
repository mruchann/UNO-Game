package tr.edu.metu.ceng.uno.card;

import java.util.List;

public enum CardColor {
    BLUE, GREEN, RED, YELLOW, NONE;

    public static List<CardColor> getColors() {
        return List.of(BLUE, GREEN, RED, YELLOW);
    }

    @Override
    public String toString() {
        return name();
    }
}
