package is.hi.hbv501g.loot.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Service.InventoryService;
import is.hi.hbv501g.loot.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CardController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InventoryService inventoryService; // Use the InventoryService

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("query", "");
        model.addAttribute("inventory", inventoryService.getInventory());
        return "search";
    }

    @PostMapping("/search")
    public String searchCards(@RequestParam String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a search query.");
            return "search";
        }

        query = "name:\"" + query.trim() + "\"";
        String url = "https://api.scryfall.com/cards/search";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("q", query);

        URI uri = builder.build().encode().toUri();
        System.out.println("Requesting URI: " + uri.toString());

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            String responseBody = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            List<Card> cards = new ArrayList<>();
            JsonNode root = mapper.readTree(responseBody);
            if (root.has("data")) {
                JsonNode data = root.get("data");
                for (JsonNode node : data) {
                    Card card = new Card();
                    card.setName(node.get("name").asText());
                    card.setMana_cost(node.has("mana_cost") ? node.get("mana_cost").asText() : "N/A");
                    card.setType_line(node.has("type_line") ? node.get("type_line").asText() : "N/A");
                    card.setOracle_text(node.has("oracle_text") ? node.get("oracle_text").asText() : "N/A");
                    if (node.has("image_uris")) {
                        card.setImageUrl(node.get("image_uris").get("normal").asText());
                    }

                    cards.add(card);
                }
                model.addAttribute("cards", cards);
                model.addAttribute("inventory", inventoryService.getInventory());
                return "results";
            } else {
                model.addAttribute("error", "Unexpected response from Scryfall API.");
                return "search";
            }
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", "Error fetching cards.");
            return "search";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred.");
            e.printStackTrace();
            return "search";
        }
    }

    @PostMapping("/addCardToInventory")
    public String addCardToInventory(@RequestParam String cardName, @RequestParam String imageUrl) {
        System.out.println("Adding card to inventory: " + cardName);
        Card card = new Card();
        card.setName(cardName);
        card.setImageUrl(imageUrl);
        inventoryService.addCard(card); // Use InventoryService instead

        return "redirect:/inventory"; // Redirect doesn't need model
    }

    @PostMapping("/removeCardFromInventory")
    public String removeCardFromInventory(@RequestParam Long cardId) {
        System.out.println("Removing card from inventory with ID: " + cardId);
        inventoryService.removeCard(cardId); // Use InventoryService instead

        return "redirect:/inventory"; // Redirect doesn't need model
    }

    @GetMapping("/inventory")
    public String showInventory(Model model) {
        model.addAttribute("inventory", inventoryService.getInventory());
        return "inventory";
    }
}

