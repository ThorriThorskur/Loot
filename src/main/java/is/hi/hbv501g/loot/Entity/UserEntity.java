package is.hi.hbv501g.loot.Entity;

import jakarta.persistence.*;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "inventory_id", referencedColumnName = "id") // Foreign key to Inventory
    private Inventory inventory;

    // No-argument constructor
    public UserEntity() {
        this.inventory = new Inventory("Default Inventory");
    }

    // Constructor with parameters
    public UserEntity(String name, String email) {
        this.name = name;
        this.email = email;
        this.inventory = new Inventory(name + "'s Inventory");
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
