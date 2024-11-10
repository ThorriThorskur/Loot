package is.hi.hbv501g.loot.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Map;



@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

    @Id
    private String id; // ID from Scryfall API

    private String name;
    private String mana_cost; // Snake case field name
    private String type_line;
    private boolean isLegendary;
    private boolean isLand;

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

    public Card(String id, String name, String mana_cost, String type_line, String oracle_text, String imageUrl, boolean isLegendary, boolean isLand) {
        this.id = id;
        this.name = name;
        this.mana_cost = mana_cost;
        this.type_line = type_line;
        this.oracle_text = oracle_text;
        this.imageUrl = imageUrl;
        this.isLegendary = isLegendary;
        this.isLand = isLand;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManaCost() {
        return mana_cost;
    }

    public void setManaCost(String mana_cost) {
        this.mana_cost = mana_cost;
    }

    public String getTypeLine() {
        return type_line;
    }

    public void setTypeLine(String type_line) {
        this.type_line = type_line;
    }

    public String getOracleText() {
        return oracle_text;
    }

    public void setOracleText(String oracle_text) {
        this.oracle_text = oracle_text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsd() {
        return usd;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    public String getUsdFoil() {
        return usd_foil;
    }

    public void setUsdFoil(String usd_foil) {
        this.usd_foil = usd_foil;
    }

    public boolean isLegendary() {
        return type_line != null && type_line.toLowerCase().contains("legendary");
    }

    public boolean isLand() {
        return type_line != null && type_line.toLowerCase().contains("land");
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
