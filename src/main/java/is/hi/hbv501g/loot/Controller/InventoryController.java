package is.hi.hbv501g.loot.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class InventoryController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

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
            // Prepare the request payload
            List<Map<String, String>> identifiers = new ArrayList<>();
            for (Card card : cardsInInventory) {
                Map<String, String> identifier = new HashMap<>();
                identifier.put("id", card.getId());
                identifiers.add(identifier);
            }

            // Build the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("identifiers", identifiers);

            try {
                // Make the API call
                String url = "https://api.scryfall.com/cards/collection";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                String responseBody = response.getBody();

                // Parse the response
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody);
                if (root.has("data")) {
                    JsonNode data = root.get("data");
                    Map<String, Card> idToCardMap = new HashMap<>();
                    for (JsonNode node : data) {
                        Card card = mapper.treeToValue(node, Card.class);
                        idToCardMap.put(card.getId(), card);
                    }

                    // Update the cards in the inventory with the latest prices
                    for (Card card : cardsInInventory) {
                        Card latestCardData = idToCardMap.get(card.getId());
                        if (latestCardData != null) {
                            card.setUsd(latestCardData.getUsd());
                            card.setUsd_foil(latestCardData.getUsd_foil());
                        }
                    }
                } else {
                    // Handle error in response
                    model.addAttribute("error", "Failed to fetch card data.");
                    return "error";
                }
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
