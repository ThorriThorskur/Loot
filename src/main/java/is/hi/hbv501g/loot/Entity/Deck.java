package is.hi.hbv501g.loot.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private UserEntity user;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeckCard> deckCards = new ArrayList<>();

    // Default constructor
    public Deck() {
    }

    // Constructor with parameters
    public Deck(String name, UserEntity user) {
        this.name = name;
        this.user = user;
    }

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 1000000)
    private byte[] picture;

    // Getters and setters
    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserEntity getUser() {
        return user;
    }

    public List<DeckCard> getDeckCards() {
        return deckCards;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setDeckCards(List<DeckCard> deckCards) {
        this.deckCards = deckCards;
    }

    // Method to add a card to the deck
    public void addCard(Card card) {
        for (DeckCard deckCard : deckCards) {
            if (deckCard.getCard().getId().equals(card.getId())) {
                deckCard.setCount(deckCard.getCount() + 1);
                return;
            }
        }
        deckCards.add(new DeckCard(this, card, 1));
    }

    // Method to remove a card from the deck
    public void removeCard(Card card) {
        deckCards.removeIf(deckCard -> {
            if (deckCard.getCard().getId().equals(card.getId())) {
                if (deckCard.getCount() > 1) {
                    deckCard.setCount(deckCard.getCount() - 1);
                    return false;
                }
                return true;
            }
            return false;
        });
    }
}
