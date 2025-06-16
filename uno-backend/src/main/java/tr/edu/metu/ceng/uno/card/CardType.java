package tr.edu.metu.ceng.uno.card;

public enum CardType {
    DRAW_TWO, SKIP, REVERSE, //draw two will appear at the beginning of the list
    
    NUMBER,

    WILD, WILD_SKIP_EVERYONE_ONCE, WILD_DRAW_FOUR;

    @Override
    public String toString() {
        return name();
    }
}
