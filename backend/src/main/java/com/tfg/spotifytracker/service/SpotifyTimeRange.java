package com.tfg.spotifytracker.service;

import java.util.Locale;

public enum SpotifyTimeRange {
    SHORT_TERM("short_term"),
    MEDIUM_TERM("medium_term"),
    LONG_TERM("long_term");

    private final String apiValue;

    SpotifyTimeRange(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

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