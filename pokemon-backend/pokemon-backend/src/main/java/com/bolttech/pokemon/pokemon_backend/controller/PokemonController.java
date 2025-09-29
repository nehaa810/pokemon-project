package com.bolttech.pokemon.pokemon_backend.controller;

import com.bolttech.pokemon.pokemon_backend.model.Pokemon;
import com.bolttech.pokemon.pokemon_backend.service.PokemonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for Pokémon API endpoints
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {
    private final PokemonService pokemonService;

    // Constructor injection
    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    // Pagination endpoint for infinite scroll
    @GetMapping
    public List<Pokemon> getPokemons(@RequestParam int page, @RequestParam int size) {
        return pokemonService.fetchPokemonPage(page, size);
    }

    // Get details of a specific Pokémon by id
    @GetMapping("/{id}")
    public Pokemon getPokemonById(@PathVariable int id) {
        return pokemonService.fetchPokemonById(id);
    }
}
