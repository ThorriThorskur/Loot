package is.hi.hbv501g.loot.Entity;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private String name;
    private List<Card> cards;

    public Inventory(String name) {
        this.name = name;
        this.cards = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }
}
