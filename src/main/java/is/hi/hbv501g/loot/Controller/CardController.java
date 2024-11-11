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
            // Fetch card types and sets
            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> cardTypesResponse = restTemplate.getForObject(cardTypesUrl, Map.class);
            List<String> cardTypes = cardTypesResponse != null && cardTypesResponse.containsKey("data")
                    ? (List<String>) cardTypesResponse.get("data")
                    : new ArrayList<>();

            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> setsResponse = restTemplate.getForObject(setsUrl, Map.class);
            List<Map<String, String>> sets = setsResponse != null && setsResponse.containsKey("data")
                    ? (List<Map<String, String>>) setsResponse.get("data")
                    : new ArrayList<>();

            model.addAttribute("cardTypes", cardTypes);
            model.addAttribute("sets", sets);
            model.addAttribute("username", userDetails != null ? userDetails.getUsername() : "Guest");

        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e, model);
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
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "isLegendary", required = false) Boolean isLegendary,
            @RequestParam(value = "isLand", required = false) Boolean isLand,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page, // Pagination
            Model model) {

        StringBuilder urlBuilder = new StringBuilder("https://api.scryfall.com/cards/search?q=");
        if (!query.trim().isEmpty()) urlBuilder.append(query.trim());

        if (type != null && !type.isEmpty()) urlBuilder.append("+t:").append(type);
        if (set != null && !set.isEmpty()) urlBuilder.append("+e:").append(set);
        if (color != null && !color.isEmpty()) urlBuilder.append("+c:").append(color);
        if (rarity != null && !rarity.isEmpty()) urlBuilder.append("+r:").append(rarity);
        if (isLegendary != null && isLegendary) urlBuilder.append("+t:legendary");
        if (isLand != null && isLand) urlBuilder.append("+t:land");

        // Add page parameter for pagination
        urlBuilder.append("&page=").append(page);
        String url = urlBuilder.toString();

        try {
            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            List<Card> cards = new ArrayList<>();
            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                for (Map<String, Object> cardData : data) {
                    Card card = new Card();
                    card.setName((String) cardData.get("name"));
                    card.setManaCost((String) cardData.get("mana_cost"));
                    card.setTypeLine((String) cardData.get("type_line"));
                    card.setOracleText((String) cardData.get("oracle_text"));

                    // Safely handle prices and image URIs
                    Map<String, Object> pricesMap = (Map<String, Object>) cardData.get("prices");
                    Map<String, Object> imageUrisMap = (Map<String, Object>) cardData.get("image_uris");

                    card.setUsd(pricesMap != null ? (String) pricesMap.get("usd") : null);
                    card.setUsdFoil(pricesMap != null ? (String) pricesMap.get("usd_foil") : null);
                    card.setImageUrl(imageUrisMap != null ? (String) imageUrisMap.get("normal") : null);

                    cards.add(card);
                }
            } else {
                model.addAttribute("error", "No cards matched your search criteria. Please try again with different terms.");
            }

            model.addAttribute("cards", cards);
            model.addAttribute("currentPage", page);
            model.addAttribute("hasMorePages", response != null && Boolean.TRUE.equals(response.get("has_more")));

        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e, model);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Make sure to return the "results" view to display the search results
        return "results";
    }

    private void handleHttpClientErrorException(HttpClientErrorException e, Model model) {
        if (e.getStatusCode().value() == 429) {
            System.err.println("Rate limit exceeded: " + e.getMessage());
            model.addAttribute("error", "Rate limit exceeded. Please try again later.");
        } else {
            System.err.println("Error fetching data from Scryfall API: " + e.getMessage());
            model.addAttribute("error", "There was an issue fetching data from Scryfall. Please try again later.");
        }
    }
}
