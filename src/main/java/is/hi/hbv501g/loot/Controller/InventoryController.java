package is.hi.hbv501g.loot.Controller;


import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.InventoryCard;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.CardService;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class InventoryController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;


    /**
     * Displays the inventory of a specific user, with an option to sort the inventory.
     *
     * @param userId The ID of the user whose inventory is to be viewed.
     * @param sortBy The field by which the inventory should be sorted (optional).
     * @param model  The model to hold attributes for the view.
     * @return The name of the Thymeleaf template to render the user's inventory.
     */
    @GetMapping("/user/{userId}/inventory")
    public String viewInventory(@PathVariable Long userId, @RequestParam(value = "sortBy", required = false) String sortBy, Model model) {
        System.out.println("Received request to view inventory for user: " + userId);
        System.out.println("Sort By: " + sortBy);
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        List<InventoryCard> inventoryCards;

        if ("mana_cost".equalsIgnoreCase(sortBy)) {
            inventoryCards = user.getInventory().getCardsSortedByManaCost();
        } else if ("name".equalsIgnoreCase(sortBy)) {
            inventoryCards = user.getInventory().getCardsSortedByName();
        } else {
            inventoryCards = user.getInventory().getInventoryCards();
        }

        model.addAttribute("user", user);
        model.addAttribute("inventoryCards", inventoryCards);
        return "user_inventory";
    }


    /**
     * Increments the quantity of a specific card in the user's inventory.
     *
     * @param userId The ID of the user whose card quantity is to be incremented.
     * @param cardId The ID of the card to be incremented.
     * @param model  The model to hold attributes for the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/user/{userId}/inventory/incrementCard")
    public String incrementCardQuantity(@PathVariable Long userId, @RequestParam("cardId") String cardId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Optional<InventoryCard> inventoryCardOptional = user.getInventory().getInventoryCards().stream()
                .filter(inventoryCard -> inventoryCard.getCard().getId().equals(cardId))
                .findFirst();

        if (inventoryCardOptional.isPresent()) {
            InventoryCard inventoryCard = inventoryCardOptional.get();
            inventoryCard.setCount(inventoryCard.getCount() + 1);
            userService.save(user); // Save updated user with inventory changes
        } else {
            model.addAttribute("error", "Card not found in inventory.");
            return "error";
        }

        return "redirect:/user/" + userId + "/inventory";
    }

    /**
     * Removes a specific card from the user's inventory. If the quantity is greater than one, it decrements the count;
     * otherwise, it removes the card entirely from the inventory.
     *
     * @param userId The ID of the user whose card is to be removed.
     * @param cardId The ID of the card to be removed.
     * @param model  The model to hold attributes for the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/user/{userId}/inventory/removeCard")
    public String removeCardFromInventory(@PathVariable Long userId, @RequestParam("cardId") String cardId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        // Fetch the user's inventory
        List<InventoryCard> inventoryCards = user.getInventory().getInventoryCards();

        // Find the card in the inventory
        Optional<InventoryCard> inventoryCardOptional = inventoryCards.stream()
                .filter(inventoryCard -> inventoryCard.getCard().getId().equals(cardId))
                .findFirst();

        if (inventoryCardOptional.isPresent()) {
            InventoryCard inventoryCard = inventoryCardOptional.get();

            // If there is more than one of the same card, decrease the count
            if (inventoryCard.getCount() > 1) {
                inventoryCard.setCount(inventoryCard.getCount() - 1);
            } else {
                // Remove the card from the inventory if only one is left
                inventoryCards.remove(inventoryCard);
            }

            userService.save(user);
        } else {
            model.addAttribute("error", "Card not found in inventory.");
            return "error";
        }

        return "redirect:/user/" + userId + "/inventory";
    }

    @GetMapping("/user/{userId}/inventory/search")
    public String searchInventory(
            @PathVariable Long userId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "isLegendary", required = false) Boolean isLegendary,
            @RequestParam(value = "isLand", required = false) Boolean isLand,
            Model model) {

        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        List<InventoryCard> inventoryCards = user.getInventory().getInventoryCards();

        // Apply search filters to the inventory
        List<InventoryCard> filteredCards = inventoryCards.stream()
                .filter(card -> {
                    boolean matches = true;

                    if (query != null && !query.isEmpty()) {
                        matches &= card.getCard().getName().toLowerCase().contains(query.toLowerCase());
                    }
                    if (color != null && !color.isEmpty()) {
                        matches &= card.getCard().getManaCost() != null && card.getCard().getManaCost().contains(color);
                    }
                    if (type != null && !type.isEmpty()) {
                        matches &= card.getCard().getTypeLine() != null && card.getCard().getTypeLine().toLowerCase().contains(type.toLowerCase());
                    }
                    if (isLegendary != null && isLegendary) {
                        matches &= card.getCard().isLegendary();
                    }
                    if (isLand != null && isLand) {
                        matches &= card.getCard().isLand();
                    }

                    return matches;
                })
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("inventoryCards", filteredCards);
        return "user_inventory";
    }

}

