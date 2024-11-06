package is.hi.hbv501g.loot.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import is.hi.hbv501g.loot.Entity.Card;
import is.hi.hbv501g.loot.Entity.InventoryCard;
import is.hi.hbv501g.loot.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;


@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Card> fetchCardsData(List<InventoryCard> inventoryCards) {
        List<Map<String, String>> identifiers = new ArrayList<>();
        for (InventoryCard inventoryCard : inventoryCards) {
            Map<String, String> identifier = new HashMap<>();
            identifier.put("id", inventoryCard.getCard().getId());
            identifiers.add(identifier);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("identifiers", identifiers);

        try {
            String url = "https://api.scryfall.com/cards/collection";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseBody = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (root.has("data")) {
                JsonNode data = root.get("data");
                Map<String, Card> idToCardMap = new HashMap<>();
                for (JsonNode node : data) {
                    Card card = mapper.treeToValue(node, Card.class);
                    idToCardMap.put(card.getId(), card);
                }
                return idToCardMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public Optional<Card> findById(String id) {
        return cardRepository.findById(id);
    }

    public Card save(Card card) {
        return cardRepository.save(card);
    }

    public void deleteById(String id) {
        cardRepository.deleteById(id);
    }

    public Optional<Card> findByName(String name) {
        return cardRepository.findByName(name);
    }
}
