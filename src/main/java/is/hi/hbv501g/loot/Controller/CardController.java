package is.hi.hbv501g.loot.Controller;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class CardController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    private static final int REQUEST_DELAY_MS = 100; // Delay between requests in milliseconds

    @GetMapping("/search")
    public String searchCards(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String cardTypesUrl = "https://api.scryfall.com/catalog/card-types";
        String setsUrl = "https://api.scryfall.com/sets";

        try {
            // Delay between requests
            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> cardTypesResponse = restTemplate.getForObject(cardTypesUrl, Map.class);
            List<String> cardTypes = cardTypesResponse != null && cardTypesResponse.containsKey("static/data")
                    ? (List<String>) cardTypesResponse.get("static/data")
                    : new ArrayList<>();

            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> setsResponse = restTemplate.getForObject(setsUrl, Map.class);
            List<Map<String, String>> sets = setsResponse != null && setsResponse.containsKey("static/data")
                    ? (List<Map<String, String>>) setsResponse.get("static/data")
                    : new ArrayList<>();

            model.addAttribute("cardTypes", cardTypes);
            model.addAttribute("sets", sets);
            model.addAttribute("username", userDetails.getUsername());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) { // Handle rate limit error
                System.err.println("Rate limit exceeded: " + e.getMessage());
                model.addAttribute("error", "Rate limit exceeded. Please try again later.");
            } else {
                System.err.println("Error fetching data from Scryfall API: " + e.getMessage());
                model.addAttribute("error", "There was an issue fetching data from Scryfall. Please try again later.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "search";
    }

    @PostMapping("/search")
    public String performSearch(
            @RequestParam("query") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "set", required = false) String set,
            @RequestParam(value = "color", required = false) String color, // Add color parameter
            @RequestParam(value = "rarity", required = false) String rarity, // Add rarity parameter
            @RequestParam(value = "isLegendary", required = false) Boolean isLegendary,
            @RequestParam(value = "isLand", required = false) Boolean isLand,
            Model model) {

        StringBuilder urlBuilder = new StringBuilder("https://api.scryfall.com/cards/search?q=");
        urlBuilder.append(query);

        // Append card type filter
        if (type != null && !type.isEmpty()) {
            urlBuilder.append("+t:").append(type);
        }

        // Append set filter
        if (set != null && !set.isEmpty()) {
            urlBuilder.append("+e:").append(set);
        }

        // Append color filter
        if (color != null && !color.isEmpty()) {
            urlBuilder.append("+c:").append(color);
        }

        // Append rarity filter
        if (rarity != null && !rarity.isEmpty()) {
            urlBuilder.append("+r:").append(rarity);
        }

        // Append legendary filter
        if (isLegendary != null && isLegendary) {
            urlBuilder.append("+t:legendary");
        }

        // Append land filter
        if (isLand != null && isLand) {
            urlBuilder.append("+t:land");
        }

        String url = urlBuilder.toString();

        try {
            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            List<Card> cards = new ArrayList<>();
            if (response != null && response.containsKey("static/data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("static/data");
                for (Map<String, Object> cardData : data) {
                    try {
                        Thread.sleep(REQUEST_DELAY_MS);
                        Card card = restTemplate.getForObject((String) cardData.get("uri"), Card.class);
                        if (card != null) {
                            cards.add(card);
                        }
                    } catch (HttpClientErrorException e) {
                        if (e.getStatusCode().value() == 429) {
                            System.err.println("Rate limit exceeded when fetching card details: " + e.getMessage());
                            model.addAttribute("error", "Rate limit exceeded when fetching card details. Please try again later.");
                            break;
                        }
                    }
                }
            }

            model.addAttribute("cards", cards);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                System.err.println("Rate limit exceeded: " + e.getMessage());
                model.addAttribute("error", "Rate limit exceeded. Please try again later.");
            } else {
                System.err.println("Error fetching search results from Scryfall API: " + e.getMessage());
                model.addAttribute("error", "There was an issue fetching data from Scryfall. Please try again later.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("API URL: " + url);
        return "results";
    }
}
