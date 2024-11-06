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

@Controller
public class InventoryController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

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

        // Apply sorting if sortBy parameter is provided
        if ("manaCost".equalsIgnoreCase(sortBy)) {
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

}

