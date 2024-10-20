package is.hi.hbv501g.loot.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Map;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

    @Id
    private String id;// ID from Scryfall API

    private String name;
    private String mana_cost;
    private String type_line;

    @Transient
    private String usd;

    @Transient
    private String usd_foil;

    @Lob
    private String oracle_text;

    private String imageUrl;

    // Constructors
    public Card() {
    }

    public Card(String id, String name, String mana_cost, String type_line, String oracle_text, String imageUrl) {
        this.id = id;
        this.name = name;
        this.mana_cost = mana_cost;
        this.type_line = type_line;
        this.oracle_text = oracle_text;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMana_cost() {
        return mana_cost;
    }

    public String getType_line() {
        return type_line;
    }

    public String getOracle_text() {
        return oracle_text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUsd() {
        return usd;
    }

    public String getUsd_foil() {
        return usd_foil;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMana_cost(String mana_cost) {
        this.mana_cost = mana_cost;
    }

    public void setType_line(String type_line) {
        this.type_line = type_line;
    }

    public void setOracle_text(String oracle_text) {
        this.oracle_text = oracle_text;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    public void setUsd_foil(String usd_foil) {
        this.usd_foil = usd_foil;
    }

    // Unpack methods for JSON deserialization
    @JsonProperty("image_uris")
    private void unpackImageUris(Map<String, String> image_uris) {
        if (image_uris != null) {
            this.imageUrl = image_uris.get("normal");
        }
    }

    @JsonProperty("prices")
    private void unpackPrices(Map<String, String> prices) {
        if (prices != null) {
            this.usd = prices.get("usd");
            this.usd_foil = prices.get("usd_foil");
        }
    }
}
