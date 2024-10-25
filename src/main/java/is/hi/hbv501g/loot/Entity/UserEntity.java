package is.hi.hbv501g.loot.Entity;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String password;

    @OneToOne(cascade = CascadeType.ALL)
    private Inventory inventory;

    // No-argument constructor
    public UserEntity() {
        this.inventory = new Inventory("Default Inventory");
    }

    // Constructor with parameters
    public UserEntity(String name, String password) {
        this.username = name;
        this.password = password;
        this.inventory = new Inventory(name + "'s Inventory");
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
