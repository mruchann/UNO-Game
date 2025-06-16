package tr.edu.metu.ceng.uno.game;

public enum Direction {
    CLOCKWISE,
    COUNTER_CLOCKWISE;

    public Direction reverse(){
        return this == CLOCKWISE ? COUNTER_CLOCKWISE : CLOCKWISE;
    }
}
