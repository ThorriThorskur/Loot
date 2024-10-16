package is.hi.hbv501g.loot.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    private Inventory inventory = new Inventory("My Inventory"); // Temporary Inventory instance

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("query", "");
        model.addAttribute("inventory", inventory);
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
                    Card card = mapper.treeToValue(node, Card.class);
                    cards.add(card);
                }
                model.addAttribute("cards", cards);
                model.addAttribute("inventory", inventory);
                return "results";
            } else if (root.has("object") && "error".equals(root.get("object").asText())) {
                String errorMessage = root.get("details").asText();
                model.addAttribute("error", errorMessage);
                return "search";
            } else {
                model.addAttribute("error", "Unexpected response from Scryfall API.");
                return "search";
            }
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode root = mapper.readTree(responseBody);
                String errorMessage = root.has("details") ? root.get("details").asText() : "Unknown error occurred.";
                model.addAttribute("error", errorMessage);
            } catch (Exception ex) {
                model.addAttribute("error", "Error parsing error response from Scryfall API.");
            }
            return "search";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred.");
            e.printStackTrace();
            return "search";
        }
    }

    @PostMapping("/addCardToInventory")
    public String addCardToInventory(@RequestParam String cardName, Model model) {
        System.out.println("Adding card to inventory: " + cardName);
        Card cardToAdd = null;
        for (Card card : inventory.getCards()) {
            System.out.println("Checking card: " + card.getName());
            if (card.getName().equalsIgnoreCase(cardName)) {
                cardToAdd = card;
                break;
            }
        }
        if (cardToAdd == null) {
            System.out.println("Card not found in existing inventory, adding new card.");
            cardToAdd = new Card();
            cardToAdd.setName(cardName);
            inventory.addCard(cardToAdd);
        } else {
            System.out.println("Card already in inventory.");
        }
        model.addAttribute("inventory", inventory);
        return "inventory"; // Ensure you have a view to display the inventory
    }

}
