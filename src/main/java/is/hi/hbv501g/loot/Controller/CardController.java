package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.Inventory;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.CardService;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class CardController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService; // Inject the new CardService

    /**
     * Displays the search page.
     * @param userId User ID for the current user.
     * @param model  Model to store attributes.
     * @return The search view.
     */
    @GetMapping("/search")
    public String searchCards(@RequestParam("userId") Long userId, Model model) {
        model.addAttribute("userId", userId);
        return "search";
    }

    /**
     * Handles the card search request from the user.
     * @param query  The search query.
     * @param userId The ID of the user performing the search.
     * @param model  Model to store attributes for the view.
     * @return The results view.
     */
    @PostMapping("/search")
    public String performSearch(@RequestParam("query") String query, @RequestParam("userId") Long userId, Model model) {
        // Use CardService to search cards
        List<Card> cards = cardService.searchCards(query);
        model.addAttribute("cards", cards);
        model.addAttribute("userId", userId);
        return "results";
    }

    /**
     * Adds a card to the user's inventory.
     * @param cardId The ID of the card to add.
     * @param userId The ID of the user whose inventory is being updated.
     * @param model  Model to store attributes for the view.
     * @return Redirects to the user's inventory view.
     */
    @PostMapping("/addCardToInventory")
    public String addCardToInventory(@RequestParam("cardId") String cardId, @RequestParam("userId") Long userId, Model model) {
        // Fetch the user and inventory
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Inventory inventory = user.getInventory();

        // Use CardService to fetch card details (from cache or API)
        Card card = cardService.fetchCardById(cardId);

        // Add the card to the user's inventory and save the user
        inventory.addCard(card);
        userService.save(user);

        return "redirect:/user/" + userId + "/inventory";
    }

    /**
     * Removes a card from the user's inventory.
     * @param cardId The ID of the card to remove.
     * @param userId The ID of the user whose inventory is being updated.
     * @param model  Model to store attributes for the view.
     * @return Redirects to the user's inventory view.
     */
    @PostMapping("/removeCardFromInventory")
    public String removeCardFromInventory(@RequestParam("cardId") String cardId, @RequestParam("userId") Long userId, Model model) {
        // Fetch the user and inventory
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Inventory inventory = user.getInventory();

        // Remove the card from the inventory and save the user
        inventory.getCards().removeIf(card -> card.getId().equals(cardId));
        userService.save(user);

        return "redirect:/user/" + userId + "/inventory";
    }
}
