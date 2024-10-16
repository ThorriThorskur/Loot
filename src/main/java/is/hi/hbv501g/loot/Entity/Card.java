package is.hi.hbv501g.loot.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    private String name;
    private String mana_cost;
    private String type_line;
    private String oracle_text;

    // Getters and Setters
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
}

