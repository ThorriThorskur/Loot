package is.hi.hbv501g.loot.Service;

import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * CardService handles all logic related to fetching, updating, and caching card data.
 * It interacts with the API for real-time data and CardRepository for local persistence.
 */
@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    private final RestTemplate restTemplate;

    /**
     * Constructor for CardService that injects RestTemplateBuilder to create RestTemplate.
     * @param builder RestTemplateBuilder for custom settings like headers.
     */
    public CardService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .defaultHeader(HttpHeaders.USER_AGENT, "Loot/1.0 stt27@hi.is")
                .build();
    }

    /**
     * Fetches a card by its ID. First checks if the card exists in the local database.
     * If not found locally, fetches the card data from the API.
     *
     * @param cardId ID of the card to fetch.
     * @return Card object, either from the local database or the API.
     */
    public Card fetchCardById(String cardId) {
        // Check if card is cached in the local database
        Optional<Card> cachedCard = cardRepository.findById(cardId);
        if (cachedCard.isPresent()) {
            return cachedCard.get();
        }

        // If not found, fetch card from Scryfall API
        String url = "https://api.scryfall.com/cards/" + cardId;
        Card card = restTemplate.getForObject(url, Card.class);

        // Save card to local database for future use
        cardRepository.save(card);

        return card;
    }

    /**
     * Updates prices of all cards in a given list of cards.
     * Makes a batch request to the API to update USD and USD foil prices.
     *
     * @param cards List of cards to update prices for.
     * @throws Exception if there's an error processing the API response.
     */
    public void updateCardPrices(List<Card> cards) throws Exception {
        if (cards.isEmpty()) {
            return; // No cards to update
        }

        // Prepare the request body for the Scryfall API batch request
        List<Map<String, String>> identifiers = new ArrayList<>();
        for (Card card : cards) {
            Map<String, String> identifier = new HashMap<>();
            identifier.put("id", card.getId());
            identifiers.add(identifier);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("identifiers", identifiers);

        String url = "https://api.scryfall.com/cards/collection";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Make the API request
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        String responseBody = response.getBody();

        // Parse the API response and update prices
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        if (root.has("data")) {
            JsonNode data = root.get("data");
            Map<String, Card> idToCardMap = new HashMap<>();
            for (JsonNode node : data) {
                Card updatedCard = mapper.treeToValue(node, Card.class);
                idToCardMap.put(updatedCard.getId(), updatedCard);
            }

            // Update the local card entities with the latest price info
            for (Card card : cards) {
                Card latestCardData = idToCardMap.get(card.getId());
                if (latestCardData != null) {
                    card.setUsd(latestCardData.getUsd());
                    card.setUsd_foil(latestCardData.getUsd_foil());
                    cardRepository.save(card); // Save updated card to local database
                }
            }
        } else {
            throw new Exception("Failed to fetch price updates.");
        }
    }

    /**
     * Saves a card to the local database. Can be used for adding new cards to an inventory.
     *
     * @param card Card object to save.
     */
    public void saveCard(Card card) {
        cardRepository.save(card);
    }

    /**
     * Deletes a card from the local database using its ID.
     *
     * @param cardId ID of the card to delete.
     */
    public void deleteCardById(String cardId) {
        cardRepository.deleteById(cardId);
    }

    /**
     * Searches cards based on a query by calling the API and returns a list of matching cards.
     * @param query The search query string.
     * @return List of matching cards from the API.
     */
    public List<Card> searchCards(String query) {
        // First, check if cards matching the query are already in the local database
        List<Card> cachedCards = cardRepository.findByName(query);

        if (!cachedCards.isEmpty()) {
            return cachedCards; // Return cached cards if found
        }

        // If not found in the database, fetch from Scryfall API
        String url = "https://api.scryfall.com/cards/search?q=" + query;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Card> cards = new ArrayList<>();
        if (response != null && response.containsKey("data")) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            for (Map<String, Object> cardData : data) {
                Card card = restTemplate.getForObject((String) cardData.get("uri"), Card.class);
                cards.add(card);
                // Save each card to the local repository
                cardRepository.save(card);
            }
        }

        return cards;
    }
}

