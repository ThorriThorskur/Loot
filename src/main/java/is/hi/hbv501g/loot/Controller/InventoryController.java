package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.CardService;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class InventoryController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService; // Inject CardService to handle card-related logic

    /**
     * Displays the user's inventory, including updated card prices.
     * @param userId ID of the user.
     * @param model  Model to pass data to the view.
     * @return View name for user inventory or an error page.
     */
    @GetMapping("/user/{userId}/inventory")
    public String viewInventory(@PathVariable Long userId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        List<Card> cardsInInventory = user.getInventory().getCards();

        if (!cardsInInventory.isEmpty()) {
            try {
                // Use CardService to update card prices in the inventory
                cardService.updateCardPrices(cardsInInventory);
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "An error occurred while fetching card data.");
                return "error";
            }
        }

        model.addAttribute("user", user);
        return "user_inventory";
    }
}
