package com.tfg.spotifytracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
/**
 * Clase funcional: SpotifyApiException.
 * Representa un error controlado de la aplicacion.
 * Se conecta con: servicios, controladores y manejador global de errores.
 */
public class SpotifyApiException extends RuntimeException {
    private final Integer statusCode;
    private final Integer retryAfterSeconds;
    private final String spotifyErrorCode;
    private final String spotifyErrorCategory;

    public SpotifyApiException(String message) {
        this(message, null, null, null, null);
    }

    public SpotifyApiException(String message, Throwable cause) {
        this(message, null, null, null, cause);
    }

    public SpotifyApiException(String message, Integer statusCode) {
        this(message, statusCode, null, null, null);
    }

    public SpotifyApiException(String message, Integer statusCode, Throwable cause) {
        this(message, statusCode, null, null, cause);
    }

    public SpotifyApiException(String message,
                               Integer statusCode,
                               Integer retryAfterSeconds,
                               String spotifyErrorCode,
                               Throwable cause) {
        this(message, statusCode, retryAfterSeconds, spotifyErrorCode, null, cause);
    }

    public SpotifyApiException(String message,
                               Integer statusCode,
                               Integer retryAfterSeconds,
                               String spotifyErrorCode,
                               String spotifyErrorCategory,
                               Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.retryAfterSeconds = retryAfterSeconds;
        this.spotifyErrorCode = spotifyErrorCode;
        this.spotifyErrorCategory = spotifyErrorCategory;
    }

    /** Devuelve un dato concreto de esta clase. */
    public Integer getStatusCode() {
        return statusCode;
    }

    /** Devuelve un dato concreto de esta clase. */
    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /** Devuelve un dato concreto de esta clase. */
    public String getSpotifyErrorCode() {
        return spotifyErrorCode;
    }

    /** Devuelve un dato concreto de esta clase. */
    public String getSpotifyErrorCategory() {
        return spotifyErrorCategory;
    }
}
