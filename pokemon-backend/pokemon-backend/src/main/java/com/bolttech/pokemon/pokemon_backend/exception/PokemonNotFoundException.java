package com.bolttech.pokemon.pokemon_backend.exception;

public class PokemonNotFoundException extends RuntimeException {
    public PokemonNotFoundException(int id) {
        super("Pokemon with ID " + id + " not found");
    }
}
