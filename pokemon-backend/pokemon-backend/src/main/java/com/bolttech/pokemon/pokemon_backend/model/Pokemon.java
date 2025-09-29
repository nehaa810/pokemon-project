package com.bolttech.pokemon.pokemon_backend.model;

import java.util.List;

public class Pokemon {
    private int id;
    private String name;
    private List<String> types;
    private String frontImage;
    private String backImage;
    private String region;
    private List<String> weaknesses;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }

    public String getFrontImage() { return frontImage; }
    public void setFrontImage(String frontImage) { this.frontImage = frontImage; }

    public String getBackImage() { return backImage; }
    public void setBackImage(String backImage) { this.backImage = backImage; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }
}
