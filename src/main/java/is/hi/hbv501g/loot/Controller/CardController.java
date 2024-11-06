package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.Inventory;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Entity.InventoryCard;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class CardController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/search")
    public String searchCards(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        return "search";
    }

    @PostMapping("/search")
    public String performSearch(@RequestParam("query") String query, Model model) {
        String url = "https://api.scryfall.com/cards/search?q=" + query;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Card> cards = new ArrayList<>();
        if (response != null && response.containsKey("data")) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            for (Map<String, Object> cardData : data) {
                Card card = restTemplate.getForObject((String) cardData.get("uri"), Card.class);
                cards.add(card);
            }
        }

        model.addAttribute("cards", cards);
        //model.addAttribute("userId", userId);
        return "results";
    }

    @PostMapping("/addCardToInventory")
    public String addCardToInventory(@RequestParam("cardId") String cardId, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserEntity user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        Inventory inventory = user.getInventory();

        // Fetch card details from Scryfall API or repository
        String url = "https://api.scryfall.com/cards/" + cardId;
        Card card = restTemplate.getForObject(url, Card.class);
        if (card == null) {
            model.addAttribute("error", "Card not found.");
            return "error";
        }

        // Add card to inventory and save user
        inventory.addCard(card);
        userService.save(user);

        return "redirect:/user/" + user.getId() + "/inventory";
    }

    @PostMapping("/removeCardFromInventory")
    public String removeCardFromInventory(@RequestParam("cardId") String cardId, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserEntity user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        Inventory inventory = user.getInventory();

        Optional<InventoryCard> optionalInventoryCard = inventory.getInventoryCards().stream()
                .filter(inventoryCard -> inventoryCard.getCard().getId().equals(cardId))
                .findFirst();

        if (optionalInventoryCard.isPresent()) {
            inventory.removeCard(optionalInventoryCard.get().getCard());
            userService.save(user);
        } else {
            model.addAttribute("error", "Card not found in inventory.");
            return "error";
        }

        return "redirect:/user/" + user.getId() + "/inventory";
    }

}
