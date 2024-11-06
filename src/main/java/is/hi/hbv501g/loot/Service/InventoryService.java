package is.hi.hbv501g.loot.Service;

import is.hi.hbv501g.loot.Entity.Inventory;
import is.hi.hbv501g.loot.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public Optional<Inventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public void deleteById(Long id) {
        inventoryRepository.deleteById(id);
    }
}
