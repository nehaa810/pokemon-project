package com.bolttech.pokemon.pokemon_backend.controller;

import com.bolttech.pokemon.pokemon_backend.model.Pokemon;
import com.bolttech.pokemon.pokemon_backend.service.PokemonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonControllerTest {

    @Mock
    private PokemonService pokemonService;

    private PokemonController pokemonController;

    @BeforeEach
    void setUp() {
        // Use constructor injection for test
        pokemonController = new PokemonController(pokemonService);
    }

    @Test
    void testGetPokemonsReturnsList() {
        Pokemon p = createPokemon(1, "Bulbasaur");
        when(pokemonService.fetchPokemonPage(0, 1)).thenReturn(List.of(p));

        List<Pokemon> result = pokemonController.getPokemons(0, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bulbasaur", result.get(0).getName());
        verify(pokemonService, times(1)).fetchPokemonPage(0, 1);
    }

    @Test
    void testGetPokemonByIdReturnsPokemon() {
        Pokemon p = createPokemon(25, "Pikachu");
        when(pokemonService.fetchPokemonById(25)).thenReturn(p);

        Pokemon result = pokemonController.getPokemonById(25);

        assertNotNull(result);
        assertEquals(25, result.getId());
        assertEquals("Pikachu", result.getName());
        verify(pokemonService, times(1)).fetchPokemonById(25);
    }

    @Test
    void testGetPokemonsEmptyList() {
        when(pokemonService.fetchPokemonPage(100, 10)).thenReturn(List.of());

        List<Pokemon> result = pokemonController.getPokemons(100, 10);

        assertTrue(result.isEmpty());
        verify(pokemonService, times(1)).fetchPokemonPage(100, 10);
    }

    @Test
    void testGetPokemonByIdReturnsNull() {
        when(pokemonService.fetchPokemonById(999)).thenReturn(null);

        Pokemon result = pokemonController.getPokemonById(999);

        assertNull(result);
        verify(pokemonService, times(1)).fetchPokemonById(999);
    }

    private Pokemon createPokemon(int id, String name) {
        Pokemon p = new Pokemon();
        p.setId(id);
        p.setName(name);
        p.setFrontImage("front.png");
        p.setBackImage("back.png");
        p.setTypes(List.of("normal"));
        p.setRegion("Kanto");
        p.setWeaknesses(List.of("fighting"));
        return p;
    }
}
