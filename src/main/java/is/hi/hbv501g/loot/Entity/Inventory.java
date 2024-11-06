package is.hi.hbv501g.loot.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserEntity user;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryCard> inventoryCards = new ArrayList<>();

    private String name;

    // Default constructor
    public Inventory() {
    }

    // Constructor with name parameter
    public Inventory(String name) {
        this.name = name;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<InventoryCard> getInventoryCards() {
        return inventoryCards;
    }

    // Method to add a card to the inventory
    public void addCard(Card card) {
        for (InventoryCard inventoryCard : inventoryCards) {
            if (inventoryCard.getCard().getId().equals(card.getId())) {
                inventoryCard.incrementCount();
                return;
            }
        }
        inventoryCards.add(new InventoryCard(this, card, 1));
    }

    // Method to remove a card from the inventory
    public void removeCard(Card card) {
        inventoryCards.removeIf(inventoryCard -> {
            if (inventoryCard.getCard().getId().equals(card.getId())) {
                if (inventoryCard.getCount() > 1) {
                    inventoryCard.decrementCount();
                    return false;
                }
                return true;
            }
            return false;
        });
    }

    // Method to get cards sorted by mana cost
    public List<InventoryCard> getCardsSortedByManaCost() {
        return inventoryCards.stream()
                .sorted(Comparator.comparing(inventoryCard -> inventoryCard.getCard().getManaCost()))
                .collect(Collectors.toList());
    }

    // Additional methods for sorting by other fields (e.g., name)
    public List<InventoryCard> getCardsSortedByName() {
        return inventoryCards.stream()
                .sorted(Comparator.comparing(inventoryCard -> inventoryCard.getCard().getName()))
                .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
