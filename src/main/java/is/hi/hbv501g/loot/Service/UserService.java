package is.hi.hbv501g.loot.Service;

import is.hi.hbv501g.loot.Entity.Inventory;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryService inventoryService;


    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public UserEntity save(UserEntity user) {
        // Save associated inventory if it exists
        Inventory inventory = user.getInventory();
        if (inventory != null) {
            inventoryService.save(inventory);
        }
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        Optional<UserEntity> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();

            // Clear inventory cards first to avoid foreign key issues
            if (user.getInventory() != null) {
                user.getInventory().getInventoryCards().clear();
            }

            // Delete the user
            userRepository.deleteById(id);
        }
    }

}
