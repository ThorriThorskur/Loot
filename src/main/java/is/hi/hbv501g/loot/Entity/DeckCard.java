package is.hi.hbv501g.loot.Entity;

import jakarta.persistence.*;

@Entity
public class DeckCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Deck deck;

    @ManyToOne
    private Card card;

    private int count; // Number of copies in the deck

    // Default constructor
    public DeckCard() {
    }

    // Constructor with parameters
    public DeckCard(Deck deck, Card card, int count) {
        this.deck = deck;
        this.card = card;
        this.count = count;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public Card getCard() {
        return card;
    }

    public int getCount() {
        return count;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
