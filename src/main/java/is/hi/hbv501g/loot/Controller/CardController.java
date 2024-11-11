package is.hi.hbv501g.loot.Controller;
import java.util.*;
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

import jakarta.servlet.http.HttpSession;


@Controller
public class CardController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    private static final int REQUEST_DELAY_MS = 100;
    private static final int CARDS_PER_PAGE = 5;

    // Initial Search and Caching
    @PostMapping("/search")
    public String performSearch(
            @RequestParam("query") String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "set", required = false) String set,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "isLegendary", required = false) Boolean isLegendary,
            @RequestParam(value = "isLand", required = false) Boolean isLand,
            Model model, HttpSession session) {

        // Fetch and Cache Results
        StringBuilder urlBuilder = new StringBuilder("https://api.scryfall.com/cards/search?q=");
        if (!query.trim().isEmpty()) urlBuilder.append(query.trim());
        if (type != null && !type.isEmpty()) urlBuilder.append("+t:").append(type);
        if (set != null && !set.isEmpty()) urlBuilder.append("+e:").append(set);
        if (color != null && !color.isEmpty()) urlBuilder.append("+c:").append(color);
        if (rarity != null && !rarity.isEmpty()) urlBuilder.append("+r:").append(rarity);
        if (isLegendary != null && isLegendary) urlBuilder.append("+t:legendary");
        if (isLand != null && isLand) urlBuilder.append("+t:land");

        try {
            Thread.sleep(REQUEST_DELAY_MS);
            Map<String, Object> response = restTemplate.getForObject(urlBuilder.toString(), Map.class);
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

    // Paginate from cached data
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

    private Card createCardFromResponseData(Map<String, Object> cardData) {
        Card card = new Card();
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
}
