package is.hi.hbv501g.loot.repository;

import is.hi.hbv501g.loot.Entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Card, Long> {
    List<Card> findByName(String cardName); // Find card(s) by name

    void deleteByName(String name); // Delete card by name
}

