package com.bolttech.pokemon.pokemon_backend.service;

import com.bolttech.pokemon.pokemon_backend.exception.PokemonNotFoundException;
import com.bolttech.pokemon.pokemon_backend.model.Pokemon;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class PokemonService {

    private final RestTemplate restTemplate;

    private  final ObjectMapper objectMapper;

    public PokemonService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/pokemon";

//    Limit to first 150 Pokémon for MVP
    private static final int TOTAL_POKEMON = 150; // First generation only for MVP

    /**
     * Pre-load cache on app start
     */
    @PostConstruct
    public void preLoadCache() {
        fetchAllPokemon();
    }

    /**
     * Fetch all Pokémon data from PokeAPI (cached)
     */
    @Cacheable("pokemonCache")
    public List<Pokemon> fetchAllPokemon() {
        List<CompletableFuture<Pokemon>> futures = new ArrayList<>();

        for (int i = 1; i <= TOTAL_POKEMON; i++) {
            int id = i;
            futures.add(CompletableFuture.supplyAsync(() -> fetchPokemonFromAPI(id)));
        }

        // Collect results after all parallel calls finish
        List<Pokemon> pokemons = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        // Fallback if API fails completely
        if (pokemons.isEmpty()) return getPlaceholderData();

        return pokemons;
    }

    /**
     * Fetch single Pokemon from PokeAPI
     */
    private Pokemon fetchPokemonFromAPI(int id) {
        try {
            String url = POKEAPI_BASE_URL + "/" + id;
            String response = restTemplate.getForObject(url, String.class);

            JsonNode pokemonNode = objectMapper.readTree(response);

            Pokemon p = new Pokemon();
            p.setId(id);
            p.setName(capitalize(pokemonNode.get("name").asText()));

            JsonNode sprites = pokemonNode.get("sprites");
            p.setFrontImage(sprites.get("front_default").asText());
            p.setBackImage(sprites.get("back_default").asText());

            List<String> types = new ArrayList<>();
            pokemonNode.get("types").forEach(t -> types.add(t.get("type").get("name").asText()));
            p.setTypes(types);

            p.setRegion("Kanto"); // First 150 are Kanto
            p.setWeaknesses(getSimpleWeaknesses(types));

            return p;
        } catch (Exception e) {
            System.err.println("Error fetching Pokemon ID " + id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Simple weaknesses mapping for MVP
     */
    private List<String> getSimpleWeaknesses(List<String> types) {
        List<String> weaknesses = new ArrayList<>();
        for (String type : types) {
            switch (type.toLowerCase()) {
                case "fire" -> weaknesses.add("water");
                case "water" -> weaknesses.add("electric");
                case "grass" -> weaknesses.add("fire");
                case "electric" -> weaknesses.add("ground");
                case "psychic" -> weaknesses.add("ghost");
                case "rock" -> weaknesses.add("water");
                default -> weaknesses.add("normal");
            }
        }
        return weaknesses;
    }

    /**
     * Capitalize first letter
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Fallback placeholder data
     */
    private List<Pokemon> getPlaceholderData() {
        List<Pokemon> pokemons = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Pokemon p = new Pokemon();
            p.setId(i);
            p.setName("Pokemon " + i);
            p.setFrontImage("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + i + ".png");
            p.setBackImage("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/" + i + ".png");
            p.setTypes(List.of("normal"));
            p.setRegion("Unknown");
            p.setWeaknesses(List.of("fighting"));
            pokemons.add(p);
        }
        return pokemons;
    }

    /**
     * Fetch Pokémon by page for infinite scroll
     */
    public List<Pokemon> fetchPokemonPage(int page, int size) {
        List<Pokemon> all = fetchAllPokemon();
        int from = page * size;
        int to = Math.min(from + size, all.size());
        if (from >= all.size()) return new ArrayList<>();
        return all.subList(from, to);
    }

    /**
     * Fetch Pokémon by ID
     */
    public Pokemon fetchPokemonById(int id) {
        return fetchAllPokemon().stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new PokemonNotFoundException(id));
    }

    /**
     * Scheduled cache refresh every hour
     */
    @Scheduled(fixedRate = 3600000)
    @CacheEvict(value = "pokemonCache", allEntries = true)
    public void refreshCache() {
        System.out.println("Refreshing Pokemon cache...");
        fetchAllPokemon();
    }


}
