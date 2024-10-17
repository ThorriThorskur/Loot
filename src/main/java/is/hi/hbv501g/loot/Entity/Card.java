package is.hi.hbv501g.loot.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String mana_cost;
    private String type_line;
    private String oracle_text;
    //normal image URL
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


        // Getter and Setter for imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMana_cost() {
        return mana_cost;
    }

    public void setMana_cost(String mana_cost) {
        this.mana_cost = mana_cost;
    }

    public String getType_line() {
        return type_line;
    }

    public void setType_line(String type_line) {
        this.type_line = type_line;
    }

    public String getOracle_text() {
        return oracle_text;
    }

    public void setOracle_text(String oracle_text) {
        this.oracle_text = oracle_text;
    }

    // Custom setter for image_uris
    @JsonProperty("image_uris")
    private void unpackImageUris(Map<String, String> imageUris) {
        if (this.imageUrl == null) { // Only set if not already set
            this.imageUrl = imageUris.get("normal");
        }
    }

    // Custom setter for card_faces
    @JsonProperty("card_faces")
    private void unpackCardFaces(List<Map<String, Object>> cardFaces) {
        if (cardFaces != null && !cardFaces.isEmpty()) {
            Map<String, Object> firstFace = cardFaces.get(0);
            Map<String, String> imageUris = (Map<String, String>) firstFace.get("image_uris");
            if (imageUris != null && this.imageUrl == null) {
                this.imageUrl = imageUris.get("normal");
            }
        }
    }
}
