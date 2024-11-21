package is.hi.hbv501g.loot.Controller;
import java.util.*;
import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.Inventory;
import is.hi.hbv501g.loot.Entity.UserEntity;
import is.hi.hbv501g.loot.Service.CardService;
import is.hi.hbv501g.loot.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpSession;

@Controller
public class CardController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    @Autowired
    private RestTemplate restTemplate;

    private static final int REQUEST_DELAY_MS = 100;
    private static final int CARDS_PER_PAGE = 12;

    /**
     * Displays the search page for cards.
     *
     * @param model The model to pass data to the view.
     * @param userDetails The authenticated user's details.
     * @return The view template for the search page.
     */
    @GetMapping("/search")
    public String searchCards(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String cardTypesUrl = "https://api.scryfall.com/catalog/card-types";
        String setsUrl = "https://api.scryfall.com/sets";

        try {
            // Delay between requests
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

    /**
     * Performs a search for cards based on the provided filters and caches the results.
     *
     * @param query The search query.
     * @param type The card type filter.
     * @param set The card set filter.
     * @param color The card color filter.
     * @param rarity The card rarity filter.
     * @param isLegendary Filter to include only legendary cards.
     * @param isLand Filter to include only land cards.
     * @param model The model to pass data to the view.
     * @param session The current HTTP session to cache search results.
     * @return The paginated results view template.
     */
    @PostMapping("/search")
    public String performSearch(
            @RequestParam("query") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "set", required = false) String set,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "rarity", required = false) List<String> rarity,
            @RequestParam(value = "isLegendary", required = false) Boolean isLegendary,
            @RequestParam(value = "isLand", required = false) Boolean isLand,
            Model model, HttpSession session) {

        StringBuilder queryBuilder = new StringBuilder();

        if (!query.trim().isEmpty()) {
            queryBuilder.append(query.trim());
        } else {
            queryBuilder.append("*");
        }
        if (type != null && !type.isEmpty()) queryBuilder.append(" t:").append(type);
        if (set != null && !set.isEmpty()) queryBuilder.append(" e:").append(set);
        if (color != null && !color.isEmpty()) queryBuilder.append(" c:").append(color);
        if (rarity != null && !rarity.isEmpty()) {
            queryBuilder.append(" (");
            for (int i = 0; i < rarity.size(); i++) {
                if (i > 0) queryBuilder.append(" OR ");
                queryBuilder.append("r:").append(rarity.get(i));
            }
            queryBuilder.append(")");
        }
        if (isLegendary != null && isLegendary) queryBuilder.append(" t:legendary");
        if (isLand != null && isLand) queryBuilder.append(" t:land");

        String encodedQuery = URLEncoder.encode(queryBuilder.toString(), StandardCharsets.UTF_8);

        String url = "https://api.scryfall.com/cards/search?q=" + encodedQuery;

        try {
            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Card> cards = new ArrayList<>();
            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                for (Map<String, Object> cardData : data) {
                    cards.add(createCardFromResponseData(cardData));
                }
                session.setAttribute("cachedCards", cards);
            }

            int totalCards = cards.size();
            int totalPages = (int) Math.ceil((double) totalCards / CARDS_PER_PAGE);

            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", 1);

            return showPaginatedResults(1, model, session);

        } catch (HttpClientErrorException | InterruptedException e) {
            handleHttpClientErrorException(e, model);
        }

        return "results";
    }

    /**
     * Displays a specific page of search results
     *
     * @param page The page number to display.
     * @param model The model to pass data to the view.
     * @param session The current HTTP session to access cached search results.
     * @return The view template for displaying the paginated results.
     */
    @GetMapping("/search/page/{page}")
    public String showPaginatedResults(@PathVariable("page") int page, Model model, HttpSession session) {
        List<Card> cachedCards = (List<Card>) session.getAttribute("cachedCards");
        if (cachedCards == null || cachedCards.isEmpty()) {
            model.addAttribute("error", "No search results found. Please perform a search.");
            return "results";
        }

        int totalCards = cachedCards.size();
        int totalPages = (int) Math.ceil((double) totalCards / CARDS_PER_PAGE);
        int start = (page - 1) * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, totalCards);

        model.addAttribute("cards", cachedCards.subList(start, end));
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);

        return "results";
    }


    /**
     * Creates a Card object from the response data.
     *
     * @param cardData A map containing the card data from the API.
     * @return The Card object created from the response data.
     */
    private Card createCardFromResponseData(Map<String, Object> cardData) {
        Card card = new Card();
        card.setId((String) cardData.get("id")); // Make sure to correctly set the ID

        // Log a warning if the card ID is null
        if (card.getId() == null) {
            System.err.println("Warning: Card ID is null for card: " + cardData);
        }

        card.setName((String) cardData.get("name"));
        card.setManaCost((String) cardData.get("mana_cost"));
        card.setTypeLine((String) cardData.get("type_line"));
        card.setOracleText((String) cardData.get("oracle_text"));

        Map<String, Object> pricesMap = (Map<String, Object>) cardData.get("prices");
        Map<String, Object> imageUrisMap = (Map<String, Object>) cardData.get("image_uris");

        card.setUsd(pricesMap != null ? (String) pricesMap.get("usd") : null);
        card.setUsdFoil(pricesMap != null ? (String) pricesMap.get("usd_foil") : null);
        card.setImageUrl(imageUrisMap != null ? (String) imageUrisMap.get("normal") : null);

        return card;
    }


    private void handleHttpClientErrorException(Exception e, Model model) {
        model.addAttribute("error", "There was an issue fetching data from Scryfall. Please try again later.");
    }

    /**
     * Adds a card to the user's inventory.
     *
     * @param cardId The ID of the card to add.
     * @param userDetails The user's details.
     * @param model The model to pass data to the view.
     * @return Redirects to the user's inventory page.
     */
    @PostMapping("/addCardToInventory")
    public String addCardToInventory(@RequestParam("cardId") String cardId, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            model.addAttribute("error", "User not authenticated.");
            return "error";
        }

        Optional<UserEntity> userOptional = userService.findByUsername(userDetails.getUsername());
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found.");
            return "error";
        }

        UserEntity user = userOptional.get();
        Inventory inventory = user.getInventory();

        // Fetch the card from the Scryfall API or local repository
        Optional<Card> cardOptional = cardService.findById(cardId);
        Card card;
        if (cardOptional.isPresent()) {
            card = cardOptional.get();
        } else {
            // Card is not found in the local database, so fetch it from the API
            String url = "https://api.scryfall.com/cards/" + cardId;
            try {
                card = restTemplate.getForObject(url, Card.class);
                if (card == null) {
                    model.addAttribute("error", "Card not found.");
                    return "error";
                }

                // Save the card to the local database
                cardService.save(card);
            } catch (Exception e) {
                model.addAttribute("error", "Error fetching card data.");
                return "error";
            }
        }

        // Add the card to the user's inventory
        inventory.addCard(card);
        userService.save(user);

        return "redirect:/user/" + user.getId() + "/inventory";
    }


}

