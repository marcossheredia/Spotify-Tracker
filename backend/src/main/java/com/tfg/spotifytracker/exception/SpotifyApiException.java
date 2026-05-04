package com.tfg.spotifytracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
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

    public Integer getStatusCode() {
        return statusCode;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public String getSpotifyErrorCode() {
        return spotifyErrorCode;
    }

    public String getSpotifyErrorCategory() {
        return spotifyErrorCategory;
    }
}
