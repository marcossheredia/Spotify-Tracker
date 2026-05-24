package com.tfg.spotifytracker.service;

import java.util.Locale;

/**
 * Clase funcional: SpotifyTimeRange.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: String.
 */
public enum SpotifyTimeRange {
    SHORT_TERM("short_term"),
    MEDIUM_TERM("medium_term"),
    LONG_TERM("long_term");

    private final String apiValue;

    SpotifyTimeRange(String apiValue) {
        this.apiValue = apiValue;
    }

    /** Obtiene datos para esta parte del sistema. */

    public String getApiValue() {
        return apiValue;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public static SpotifyTimeRange fromQuery(String queryValue) {
        if (queryValue == null || queryValue.isBlank()) {
            return SHORT_TERM;
        }

        String normalized = queryValue.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "short_term", "short", "this_month", "month", "current_month" -> SHORT_TERM;
            case "medium_term", "medium", "last_6_months", "six_months", "semester" -> MEDIUM_TERM;
            case "long_term", "long", "all_time", "all", "historic" -> LONG_TERM;
            default -> SHORT_TERM;
        };
    }
}