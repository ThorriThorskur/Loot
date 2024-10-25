package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.Inventory;
import is.hi.hbv501g.loot.Entity.UserEntity;
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
    public String addCardToInventory(@RequestParam("cardId") String cardId, @RequestParam("userId") Long userId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }
        UserEntity user = userOptional.get();
        Inventory inventory = user.getInventory();

        // Fetch card details from Scryfall API
        String url = "https://api.scryfall.com/cards/" + cardId;
        Card card = restTemplate.getForObject(url, Card.class);

        // Add card to inventory
        inventory.addCard(card);
        userService.save(user);

        return "redirect:/user/" + userId + "/inventory";
    }

    @PostMapping("/removeCardFromInventory")
    public String removeCardFromInventory(@RequestParam("cardId") String cardId, @RequestParam("userId") Long userId, Model model) {
        Optional<UserEntity> userOptional = userService.findById(userId);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }
        UserEntity user = userOptional.get();
        Inventory inventory = user.getInventory();

        inventory.getCards().removeIf(card -> card.getId().equals(cardId));
        userService.save(user);

        return "redirect:/user/" + userId + "/inventory";
    }
}
