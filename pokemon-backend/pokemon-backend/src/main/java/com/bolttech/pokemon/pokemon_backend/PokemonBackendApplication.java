package com.bolttech.pokemon.pokemon_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application
 */
@SpringBootApplication
@EnableScheduling
public class PokemonBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PokemonBackendApplication.class, args);
    }
}
