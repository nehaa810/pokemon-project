package com.bolttech.pokemon.pokemon_backend.service;

import com.bolttech.pokemon.pokemon_backend.exception.PokemonNotFoundException;
import com.bolttech.pokemon.pokemon_backend.model.Pokemon;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode mockJsonNode;

    @Mock
    private JsonNode mockSpritesNode;

    @Mock
    private JsonNode mockTypesNode;

    private PokemonService pokemonService;

    // Capture console output to avoid spam
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String MOCK_POKEMON_JSON = """
            {
              "id": 1,
              "name": "bulbasaur",
              "sprites": {
                "front_default": "https://example.com/front.png",
                "back_default": "https://example.com/back.png"
              },
              "types": [
                { "type": { "name": "grass" } }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        // Redirect System.out and System.err to avoid console spam
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        // Use constructor injection
        pokemonService = new PokemonService(restTemplate, objectMapper);
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out and System.err
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testPreLoadCache() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));
        assertDoesNotThrow(() -> pokemonService.preLoadCache());
    }

    @Test
    void testFetchPokemonById_NotFound() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        PokemonNotFoundException exception = assertThrows(
                PokemonNotFoundException.class,
                () -> pokemonService.fetchPokemonById(999)
        );

        assertEquals("Pokemon with ID 999 not found", exception.getMessage());
    }

    @Test
    void testFetchAllPokemon_Success() throws Exception {
        // Setup successful API response
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(MOCK_POKEMON_JSON);

        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        when(mockJsonNode.get("name")).thenReturn(mockJsonNode);
        when(mockJsonNode.asText()).thenReturn("bulbasaur");
        when(mockJsonNode.get("sprites")).thenReturn(mockSpritesNode);
        when(mockSpritesNode.get("front_default")).thenReturn(mockJsonNode);
        when(mockSpritesNode.get("back_default")).thenReturn(mockJsonNode);
        when(mockJsonNode.get("types")).thenReturn(mockTypesNode);

        // Mock the forEach method for types
        doAnswer(invocation -> {
            // Simulate processing one type - no actual iteration needed
            return null;
        }).when(mockTypesNode).forEach(any());

        List<Pokemon> result = pokemonService.fetchAllPokemon();

        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }

    @Test
    void testFetchAllPokemon_APIFailure_ReturnsFallback() {
        // Mock complete API failure
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        List<Pokemon> result = pokemonService.fetchAllPokemon();

        assertNotNull(result);
        assertEquals(10, result.size()); // Fallback returns 10 placeholder Pokemon
        assertEquals("Pokemon 1", result.get(0).getName());
        assertEquals("normal", result.get(0).getTypes().get(0));
        assertEquals("Unknown", result.get(0).getRegion());
    }

    @Test
    void testFetchAllPokemon_ParseError_ReturnsFallback() throws Exception {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(MOCK_POKEMON_JSON);
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("Parse error"));

        List<Pokemon> result = pokemonService.fetchAllPokemon();

        assertNotNull(result);
        assertEquals(10, result.size()); // Should return fallback data
    }

    @Test
    void testGetSimpleWeaknesses_AllTypes() {
        // Test all switch cases in one test to reduce redundancy
        assertEquals(Arrays.asList("water"),
                ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", Arrays.asList("fire")));

        assertEquals(Arrays.asList("electric"),
                ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", Arrays.asList("water")));

        assertEquals(Arrays.asList("fire"),
                ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", Arrays.asList("grass")));

        assertEquals(Arrays.asList("ground"),
                ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", Arrays.asList("electric")));

        assertEquals(Arrays.asList("ghost"),
                ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", Arrays.asList("psychic")));

        assertEquals(Arrays.asList("water"),
                ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", Arrays.asList("rock")));

        assertEquals(Arrays.asList("normal"),
                ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", Arrays.asList("unknown")));
    }

    @Test
    void testGetSimpleWeaknesses_MultipleTypes() {
        List<String> types = Arrays.asList("fire", "water", "electric");
        List<String> result = ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", types);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("water"));
        assertTrue(result.contains("electric"));
        assertTrue(result.contains("ground"));
    }

    @Test
    void testGetSimpleWeaknesses_CaseInsensitive() {
        List<String> types = Arrays.asList("FIRE", "Water", "GrAsS");
        List<String> result = ReflectionTestUtils.invokeMethod(pokemonService, "getSimpleWeaknesses", types);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("water"));
        assertTrue(result.contains("electric"));
        assertTrue(result.contains("fire"));
    }

    @Test
    void testCapitalize_AllCases() {
        // Test all capitalize scenarios
        assertEquals("Bulbasaur",
                ReflectionTestUtils.invokeMethod(pokemonService, "capitalize", "bulbasaur"));

        assertNull(ReflectionTestUtils.invokeMethod(pokemonService, "capitalize", (String) null));

        assertEquals("",
                ReflectionTestUtils.invokeMethod(pokemonService, "capitalize", ""));

        assertEquals("A",
                ReflectionTestUtils.invokeMethod(pokemonService, "capitalize", "a"));

        assertEquals("Pikachu",
                ReflectionTestUtils.invokeMethod(pokemonService, "capitalize", "Pikachu"));
    }

    @Test
    void testGetPlaceholderData() {
        List<Pokemon> result = ReflectionTestUtils.invokeMethod(pokemonService, "getPlaceholderData");

        assertNotNull(result);
        assertEquals(10, result.size());

        // Test first Pokemon
        Pokemon first = result.get(0);
        assertEquals(1, first.getId());
        assertEquals("Pokemon 1", first.getName());
        assertEquals("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
                first.getFrontImage());
        assertEquals("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/1.png",
                first.getBackImage());
        assertEquals(Arrays.asList("normal"), first.getTypes());
        assertEquals("Unknown", first.getRegion());
        assertEquals(Arrays.asList("fighting"), first.getWeaknesses());

        // Test last Pokemon
        Pokemon last = result.get(9);
        assertEquals(10, last.getId());
        assertEquals("Pokemon 10", last.getName());
    }

    @Test
    void testFetchPokemonPage_ValidRange() {
        // Mock API failure to get fallback data
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        List<Pokemon> result = pokemonService.fetchPokemonPage(0, 5);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Pokemon 1", result.get(0).getName());
        assertEquals("Pokemon 5", result.get(4).getName());
    }

    @Test
    void testFetchPokemonPage_SecondPage() {
        // Mock API failure to get fallback data
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        List<Pokemon> result = pokemonService.fetchPokemonPage(1, 5);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Pokemon 6", result.get(0).getName());
        assertEquals("Pokemon 10", result.get(4).getName());
    }

    @Test
    void testFetchPokemonPage_OutOfRange() {
        // Mock API failure to get fallback data
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        List<Pokemon> result = pokemonService.fetchPokemonPage(10, 5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchPokemonPage_PartialLastPage() {
        // Mock API failure to get fallback data (10 total Pokemon)
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        List<Pokemon> result = pokemonService.fetchPokemonPage(1, 8);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Pokemon 9", result.get(0).getName());
        assertEquals("Pokemon 10", result.get(1).getName());
    }

    @Test
    void testFetchPokemonById_Found() {
        // Mock API failure to get fallback data
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        Pokemon result = pokemonService.fetchPokemonById(5);

        assertNotNull(result);
        assertEquals(5, result.getId());
        assertEquals("Pokemon 5", result.getName());
    }

    @Test
    void testFetchPokemonById_FirstPokemon() {
        // Mock API failure to get fallback data
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        Pokemon result = pokemonService.fetchPokemonById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Pokemon 1", result.getName());
    }

    @Test
    void testRefreshCache() {
        // Mock API failure to test the fallback path in refresh
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        assertDoesNotThrow(() -> pokemonService.refreshCache());

        // Verify console output was captured (cache refresh message)
        assertTrue(outContent.toString().contains("Refreshing Pokemon cache..."));
    }

    @Test
    void testRefreshCache_WithSuccessfulAPI() throws Exception {
        // Mock successful API response for refresh
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(MOCK_POKEMON_JSON);
        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        when(mockJsonNode.get("name")).thenReturn(mockJsonNode);
        when(mockJsonNode.asText()).thenReturn("bulbasaur");
        when(mockJsonNode.get("sprites")).thenReturn(mockSpritesNode);
        when(mockSpritesNode.get("front_default")).thenReturn(mockJsonNode);
        when(mockSpritesNode.get("back_default")).thenReturn(mockJsonNode);
        when(mockJsonNode.get("types")).thenReturn(mockTypesNode);
        doAnswer(invocation -> null).when(mockTypesNode).forEach(any());

        assertDoesNotThrow(() -> pokemonService.refreshCache());

        // Verify console output
        assertTrue(outContent.toString().contains("Refreshing Pokemon cache..."));
    }

    @Test
    void testErrorHandling_ConsoleOutput() {
        // Test that error messages are properly logged to console
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API Error"));

        pokemonService.fetchAllPokemon();

        // Verify error messages were captured
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error fetching Pokemon ID"));
        assertTrue(errorOutput.contains("API Error"));
    }
}