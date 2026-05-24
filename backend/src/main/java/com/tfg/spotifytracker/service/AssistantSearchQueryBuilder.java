package com.tfg.spotifytracker.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
/**
 * Clase funcional: AssistantSearchQueryBuilder.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras partes de la aplicación.
 */
public class AssistantSearchQueryBuilder {

    public List<String> buildQueries(String originalMessage,
                                     List<String> genreQueries,
                                     List<String> contextQueries,
                                     String language) {
        Set<String> unique = new LinkedHashSet<>();
        String normalized = normalizeForMatching(originalMessage);

        addDetectedQueries(unique, normalized);

        for (String genre : filterValues(genreQueries)) {
            unique.add(normalizeQuery(genre));
        }

        for (String context : filterValues(contextQueries)) {
            unique.add(normalizeQuery(context));
        }

        if (unique.isEmpty()) {
            unique.add("popular songs");
        }

        List<String> queries = new ArrayList<>();
        for (String query : unique) {
            if (StringUtils.hasText(query)) {
                queries.add(query);
            }
            if (queries.size() >= 3) {
                break;
            }
        }
        return queries;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private void addDetectedQueries(Set<String> unique, String normalized) {
        if (!StringUtils.hasText(normalized)) {
            return;
        }

        if (normalized.contains("rock") && (normalized.contains("70") || normalized.contains("setenta"))) {
            unique.add("classic rock 70s");
            unique.add("70s rock classics");
            unique.add("rock classics");
            return;
        }

        if (normalized.contains("disco") || normalized.contains("funk")) {
            if (normalized.contains("80") || normalized.contains("ochenta")) {
                unique.add("disco 80s classics");
                unique.add("80s dance classics");
                unique.add("funk disco 80s");
            } else {
                unique.add("disco classics");
                unique.add("dance classics");
                unique.add("funk disco");
            }
            return;
        }

        if (normalized.contains("boda") || normalized.contains("romant")) {
            unique.add("romantic wedding songs");
            unique.add("classic love songs");
            unique.add("wedding love songs");
            return;
        }

        if (normalized.contains("rock")) {
            unique.add("rock hits");
            unique.add("rock classics");
            unique.add("classic rock");
            return;
        }

        if (normalized.contains("pop")) {
            unique.add("pop hits");
            unique.add("pop songs");
            unique.add("popular pop");
            return;
        }

        if (normalized.contains("jazz")) {
            unique.add("smooth jazz");
            unique.add("jazz classics");
            unique.add("jazz songs");
            return;
        }

        if (normalized.contains("estudi") || normalized.contains("concentr")) {
            unique.add("study music");
            unique.add("focus music");
            unique.add("lofi study");
            return;
        }

        if (normalized.contains("fiesta")) {
            unique.add("party hits");
            unique.add("party classics");
            unique.add("dance party songs");
            return;
        }

        unique.add("popular songs");
        unique.add("spotify hits");
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

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

    /** Normaliza el valor de entrada para evitar errores. */

    private String normalizeQuery(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /** Normaliza el valor de entrada para evitar errores. */

    private String normalizeForMatching(String message) {
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }
}
