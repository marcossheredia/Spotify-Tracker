package com.tfg.spotifytracker.service;


import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class AssistantSearchQueryBuilder {

    public List<String> buildQueries(String originalMessage,
                                     List<String> genreQueries,
                                     List<String> contextQueries,
                                     String language) {
        Set<String> unique = new LinkedHashSet<>();
        String normalizedLanguage = language == null ? "" : language.toLowerCase(Locale.ROOT);
        boolean isSpanish = normalizedLanguage.startsWith("es");

        List<String> safeGenres = filterValues(genreQueries);
        List<String> safeContexts = filterValues(contextQueries);

        if (!safeGenres.isEmpty() && !safeContexts.isEmpty()) {
            for (String genre : safeGenres) {
                for (String context : safeContexts) {
                    unique.add(normalizeQuery(genre + " " + context));
                }
            }
        }

        if (!safeGenres.isEmpty()) {
            for (String genre : safeGenres) {
                unique.add(normalizeQuery(genre));
                unique.add(normalizeQuery((isSpanish ? "mejores " : "best ") + genre));
                unique.add(normalizeQuery((isSpanish ? "playlist " : "playlist ") + genre));
            }
        }

        if (!safeContexts.isEmpty()) {
            for (String context : safeContexts) {
                unique.add(normalizeQuery(context + " playlist"));
                unique.add(normalizeQuery(context + " mix"));
                unique.add(normalizeQuery(context + (isSpanish ? " energia" : " energy")));
            }
        }

        if (StringUtils.hasText(originalMessage)) {
            unique.add(normalizeQuery(originalMessage));
        }

        List<String> queries = new ArrayList<>();
        for (String query : unique) {
            if (StringUtils.hasText(query)) {
                queries.add(query);
            }
        }
        return queries;
    }

    private List<String> filterValues(List<String> values) {
        if (values == null) {
            return List.of();
        }

        List<String> filtered = new ArrayList<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                filtered.add(value.trim());
            }
        }
        return filtered;
    }

    private String normalizeQuery(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }
}
