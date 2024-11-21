package is.hi.hbv501g.loot.Controller;


import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.InventoryCard;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.CardService;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
     * @param sortBy The field by which the inventory should be sorted (optional).
     * @param model  The model to hold attributes for the view.
     * @return The name of the Thymeleaf template to render the user's inventory.
     */
    @GetMapping("/user/inventory")
    public String viewInventory(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(value = "sortBy", required = false) String sortBy, Model model) {
        if (userDetails == null) {
            model.addAttribute("error", "User not found.");
            return "error";
        }
        System.out.println("Received request to view inventory for user: " + userDetails.getUsername());
        System.out.println("Sort By: " + sortBy);
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());

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
     * @param cardId The ID of the card to be incremented.
     * @param quantity The new quantity of the card
     * @param model  The model to hold attributes for the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/user/inventory/quantity")
    public String incrementCardQuantity(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("cardId") String cardId, @RequestParam("quantity") String quantity, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Optional<InventoryCard> inventoryCardOptional = user.getInventory().getInventoryCards().stream()
                .filter(inventoryCard -> inventoryCard.getCard().getId().equals(cardId))
                .findFirst();
        int q = Integer.parseInt(quantity);

        if (q > 0) {
            InventoryCard inventoryCard = inventoryCardOptional.get();
            inventoryCard.setCount(q);
            userService.save(user); // Save updated user with inventory changes
        } else {
            model.addAttribute("error", "Invalid quantity size");
            return "error";
        }

        return "redirect:/user/inventory";
    }

    /**
     * Removes a specific card from the user's inventory. If the quantity is greater than one, it decrements the count;
     * otherwise, it removes the card entirely from the inventory.
     *
     * @param cardId The ID of the card to be removed.
     * @param model  The model to hold attributes for the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/user/inventory/removeCard")
    public String removeCardFromInventory(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("cardId") String cardId, Model model) {
        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

            inventoryCards.remove(inventoryCard);

            userService.save(user);
        } else {
            model.addAttribute("error", "Card not found in inventory.");
            return "error";
        }

        return "redirect:/user/inventory";
    }

    @GetMapping("/user/inventory/search")
    public String searchInventory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "isLegendary", required = false) Boolean isLegendary,
            @RequestParam(value = "isLand", required = false) Boolean isLand,
            Model model) {

        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
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

