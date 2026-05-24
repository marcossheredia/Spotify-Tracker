package com.tfg.spotifytracker;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableCaching
@EnableRetry
/**
 * Clase funcional: SpotifyTrackerApplication.
 * Soporta una parte del funcionamiento de la aplicacion.
 * Se conecta con: otras clases del proyecto.
 */
public class SpotifyTrackerApplication {
    /** Ejecuta una parte concreta de la lógica de esta clase. */
    public static void main(String[] args) {
        SpringApplication.run(SpotifyTrackerApplication.class, args);
    }
}

