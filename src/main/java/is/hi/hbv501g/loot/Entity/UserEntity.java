package is.hi.hbv501g.loot.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    private String role; // Role field to support Spring Security (e.g., "USER" or "ADMIN")

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 1000000) // Increase the length to handle larger data (e.g., 1 MB)
    private byte[] profilePicture;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "inventory_id", referencedColumnName = "id")
    private Inventory inventory;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deck> decks = new ArrayList<>();

    // No-argument constructor
    public UserEntity() {
        this.inventory = new Inventory("Default Inventory");
    }

    // Constructor with parameters
    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
        this.inventory = new Inventory(username + "'s Inventory");
        this.role = "USER"; // Default role
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public List<Deck> getDecks() {
        return decks;
    }

    public void setDecks(List<Deck> decks) {
        this.decks = decks;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    // Utility method to add a deck to the user
    public void addDeck(Deck deck) {
        deck.setUser(this);
        this.decks.add(deck);
    }
}
