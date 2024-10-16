package is.hi.hbv501g.loot.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import is.hi.hbv501g.loot.Entity.Card;
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

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("query", "");
        return "search";
    }

    @PostMapping("/search")
    public String searchCards(@RequestParam String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("error", "Please enter a search query.");
            return "search";
        }

        // Format the query
        query = "name:\"" + query.trim() + "\"";

        String url = "https://api.scryfall.com/cards/search";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("q", query);

        // Build and encode the URI
        URI uri = builder.build().encode().toUri();

        // Log the request URI
        System.out.println("Requesting URI: " + uri.toString());

        try {
            // Use the URI object here
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            String responseBody = response.getBody();

            // Parse the JSON response
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
            // Handle HTTP errors from the Scryfall API
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
            // Handle other exceptions
            model.addAttribute("error", "An unexpected error occurred.");
            e.printStackTrace(); // Optional: Log the exception for debugging
            return "search";
        }
    }

}
