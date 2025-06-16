package tr.edu.metu.ceng.uno.model;


public class Card {
    private String type;
    private String color;
    private Integer number;

    public Card(String type, String color, Integer  value) {
        this.type = type;
        this.color = color;
        this.number = value;
    }
    public Card(String type, String color) {
        this.type = type;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    //TODO: call when the card is NumberCard
    public Integer getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }
}
