package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
/**
 * Clase funcional: AssistantPlaylistPlannerService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: AssistantSearchQueryBuilder.
 */
public class AssistantPlaylistPlannerService {

    private static final int DEFAULT_TRACK_LIMIT = 25;
    private static final int MIN_TRACK_LIMIT = 5;
    private static final int MAX_TRACK_LIMIT = 25;

    private final AssistantSearchQueryBuilder assistantSearchQueryBuilder;

    /** Construye una respuesta o estructura intermedia. */

    public AssistantPlaylistPlanDTO buildPlan(String message, Integer trackLimit, Boolean publicPlaylist) {
        String safeMessage = normalizeMessage(message);
        String normalized = normalizeForMatching(safeMessage);
        int safeTrackLimit = normalizeTrackLimit(trackLimit);
        boolean safePublic = Boolean.TRUE.equals(publicPlaylist);

        List<String> genres = detectGenres(normalized);
        List<String> contexts = detectContexts(normalized);
        String language = detectLanguage(normalized);
        String playlistName = buildPlaylistName(genres, contexts, safeMessage);
        String description = "Playlist creada por Spotify Tracker. Solicitud: " + safeMessage;

        List<String> queries = assistantSearchQueryBuilder.buildQueries(
            safeMessage,
            genres,
            contexts,
            language
        );

        return AssistantPlaylistPlanDTO.builder()
            .originalMessage(safeMessage)
            .playlistName(playlistName)
            .description(description)
            .intent("playlist")
            .language(language)
            .contextTags(contexts)
            .genres(genres)
            .searchQueries(queries)
            .trackLimit(safeTrackLimit)
            .publicPlaylist(safePublic)
            .build();
    }

    /** Normaliza el valor de entrada para evitar errores. */

    private String normalizeMessage(String message) {
        return message == null ? "" : message.trim().replaceAll("\\s+", " ");
    }

    /** Normaliza el valor de entrada para evitar errores. */

    private String normalizeForMatching(String message) {
        String text = normalizeMessage(message).toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    /** Normaliza el valor de entrada para evitar errores. */

    private int normalizeTrackLimit(Integer trackLimit) {
        int limit = trackLimit == null ? DEFAULT_TRACK_LIMIT : trackLimit;
        return Math.max(MIN_TRACK_LIMIT, Math.min(MAX_TRACK_LIMIT, limit));
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private String detectLanguage(String normalized) {
        return normalized.contains("quiero") || normalized.contains("crea") || normalized.contains("hazme") ? "es" : "en";
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private List<String> detectGenres(String normalized) {
        List<String> genres = new ArrayList<>();
        addIfContains(genres, normalized, "rock", "rock");
        addIfContains(genres, normalized, "pop", "pop");
        addIfContains(genres, normalized, "disco", "disco");
        addIfContains(genres, normalized, "funk", "funk");
        addIfContains(genres, normalized, "jazz", "jazz");
        addIfContains(genres, normalized, "metal", "metal");
        addIfContains(genres, normalized, "reggaeton", "reggaeton");
        return genres;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private List<String> detectContexts(String normalized) {
        List<String> contexts = new ArrayList<>();
        addIfContains(contexts, normalized, "boda", "boda");
        addIfContains(contexts, normalized, "romant", "romantica");
        addIfContains(contexts, normalized, "estudi", "estudiar");
        addIfContains(contexts, normalized, "fiesta", "fiesta");
        addIfContains(contexts, normalized, "entren", "entrenar");
        addIfContains(contexts, normalized, "gimnasio", "entrenar");
        return contexts;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private void addIfContains(List<String> values, String text, String keyword, String value) {
        if (StringUtils.hasText(text) && text.contains(keyword) && !values.contains(value)) {
            values.add(value);
        }
    }

    /** Construye una respuesta o estructura intermedia. */

    private String buildPlaylistName(List<String> genres, List<String> contexts, String fallbackMessage) {
        if (!genres.isEmpty() && !contexts.isEmpty()) {
            return capitalize(genres.get(0)) + " para " + contexts.get(0);
        }
        if (!genres.isEmpty()) {
            return capitalize(genres.get(0)) + " mix";
        }
        if (!contexts.isEmpty()) {
            return "Musica para " + contexts.get(0);
        }
        if (StringUtils.hasText(fallbackMessage)) {
            return fallbackMessage.length() > 50 ? fallbackMessage.substring(0, 50).trim() : fallbackMessage;
        }
        return "Playlist personalizada";
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private String capitalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
