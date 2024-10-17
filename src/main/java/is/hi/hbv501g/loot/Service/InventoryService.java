package is.hi.hbv501g.loot.Service;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Card> getInventory() {
        return inventoryRepository.findAll(); // Always fetch the current inventory
    }

    public void addCard(Card card) {
        inventoryRepository.save(card); // Add a new card
    }

    public void removeCard(Long cardId) {
        inventoryRepository.findById(cardId).ifPresent(inventoryRepository::delete); // Remove the card if it exists
    }
}
