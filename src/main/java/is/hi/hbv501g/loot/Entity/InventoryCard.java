package is.hi.hbv501g.loot.Entity;

import jakarta.persistence.*;

@Entity
public class InventoryCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Inventory inventory;

    @ManyToOne
    private Card card;

    private int count;

    // Default constructor
    public InventoryCard() {
    }

    // Constructor with Inventory, Card, and count parameters
    public InventoryCard(Inventory inventory, Card card, int count) {
        this.inventory = inventory;
        this.card = card;
        this.count = count;
    }

    // Constructor without Inventory parameter (used elsewhere if needed)
    public InventoryCard(Card card, int count) {
        this.card = card;
        this.count = count;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incrementCount() {
        this.count++;
    }

    public void decrementCount() {
        if (this.count > 0) {
            this.count--;
        }
    }
}
