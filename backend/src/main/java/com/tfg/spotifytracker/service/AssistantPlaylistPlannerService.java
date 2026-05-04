package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantPlaylistPlannerService {

    private static final int DEFAULT_TRACK_LIMIT = 25;
    private static final int MIN_TRACK_LIMIT = 5;
    private static final int MAX_TRACK_LIMIT = 50;

    private final AssistantAiService assistantAiService;
    private final AssistantSearchQueryBuilder assistantSearchQueryBuilder;

    public AssistantPlaylistPlanDTO buildPlan(String message, Integer trackLimit, Boolean publicPlaylist) {
        AssistantPlaylistPlanDTO fallbackPlan = buildFallbackPlan(message, trackLimit, publicPlaylist);

        Optional<AssistantPlaylistPlanDTO> aiPlan = assistantAiService.generatePlan(message, trackLimit, publicPlaylist);
        if (aiPlan.isEmpty()) {
            return fallbackPlan;
        }

        return mergePlans(aiPlan.get(), fallbackPlan);
    }

    private AssistantPlaylistPlanDTO buildFallbackPlan(String message, Integer trackLimit, Boolean publicPlaylist) {
        String safeMessage = normalizeMessage(message);
        String normalized = normalizeForMatching(safeMessage);

        List<GenreHint> genres = detectGenres(normalized);
        List<ContextHint> contexts = detectContexts(normalized);
        String language = detectLanguage(normalized);
        String intent = detectIntent(normalized);

        int safeTrackLimit = normalizeTrackLimit(trackLimit);
        boolean safePublic = Boolean.TRUE.equals(publicPlaylist);

        List<String> genreLabels = genres.stream().map(GenreHint::displayName).toList();
        List<String> contextLabels = contexts.stream().map(ContextHint::displayName).toList();
        List<String> genreQueries = genres.stream().map(GenreHint::primaryQuery).toList();
        List<String> contextQueries = contexts.stream().map(ContextHint::primaryQuery).toList();

        String playlistName = buildPlaylistName(genreLabels, contextLabels, safeMessage);
        String description = buildDescription(playlistName, safeMessage);

        List<String> queries = assistantSearchQueryBuilder.buildQueries(
            safeMessage,
            genreQueries,
            contextQueries,
            language
        );

        if (queries.isEmpty()) {
            queries = List.of(safeMessage);
        }

        return AssistantPlaylistPlanDTO.builder()
            .originalMessage(safeMessage)
            .playlistName(playlistName)
            .description(description)
            .intent(intent)
            .language(language)
            .contextTags(contextLabels)
            .genres(genreLabels)
            .searchQueries(queries)
            .trackLimit(safeTrackLimit)
            .publicPlaylist(safePublic)
            .build();
    }

    private AssistantPlaylistPlanDTO mergePlans(AssistantPlaylistPlanDTO aiPlan, AssistantPlaylistPlanDTO fallback) {
        if (aiPlan == null) {
            return fallback;
        }

        return AssistantPlaylistPlanDTO.builder()
            .originalMessage(resolveText(aiPlan.getOriginalMessage(), fallback.getOriginalMessage()))
            .playlistName(resolveText(aiPlan.getPlaylistName(), fallback.getPlaylistName()))
            .description(resolveText(aiPlan.getDescription(), fallback.getDescription()))
            .intent(resolveText(aiPlan.getIntent(), fallback.getIntent()))
            .language(resolveText(aiPlan.getLanguage(), fallback.getLanguage()))
            .contextTags(resolveList(aiPlan.getContextTags(), fallback.getContextTags()))
            .genres(resolveList(aiPlan.getGenres(), fallback.getGenres()))
            .searchQueries(resolveList(aiPlan.getSearchQueries(), fallback.getSearchQueries()))
            .trackLimit(aiPlan.getTrackLimit() != null ? aiPlan.getTrackLimit() : fallback.getTrackLimit())
            .publicPlaylist(aiPlan.getPublicPlaylist() != null ? aiPlan.getPublicPlaylist() : fallback.getPublicPlaylist())
            .build();
    }

    private String resolveText(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    private List<String> resolveList(List<String> primary, List<String> fallback) {
        if (primary != null && !primary.isEmpty()) {
            return primary;
        }
        return fallback == null ? List.of() : fallback;
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.trim().replaceAll("\\s+", " ");
    }

    private String normalizeForMatching(String message) {
        String text = normalizeMessage(message).toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private int normalizeTrackLimit(Integer trackLimit) {
        int limit = trackLimit == null ? DEFAULT_TRACK_LIMIT : trackLimit;
        return Math.max(MIN_TRACK_LIMIT, Math.min(MAX_TRACK_LIMIT, limit));
    }

    private String detectLanguage(String normalized) {
        if (!StringUtils.hasText(normalized)) {
            return "es";
        }

        String[] spanishHints = {
            "quiero", "crea", "creame", "crear", "para", "entrenar", "estudiar", "relajar", "fiesta",
            "noche", "musica", "playlist", "cancion", "dame", "hazme", "poner"
        };

        for (String hint : spanishHints) {
            if (normalized.contains(hint)) {
                return "es";
            }
        }
        return "en";
    }

    private String detectIntent(String normalized) {
        if (!StringUtils.hasText(normalized)) {
            return "playlist";
        }

        if (normalized.contains("playlist") || normalized.contains("lista")) {
            return "playlist";
        }
        return "musica";
    }

    private List<GenreHint> detectGenres(String normalized) {
        List<GenreHint> genres = new ArrayList<>();
        for (GenreHint hint : genreHints()) {
            if (normalized.contains(hint.keyword())) {
                genres.add(hint);
            }
        }
        return genres;
    }

    private List<ContextHint> detectContexts(String normalized) {
        List<ContextHint> contexts = new ArrayList<>();
        for (ContextHint hint : contextHints()) {
            if (normalized.contains(hint.keyword())) {
                contexts.add(hint);
            }
        }
        return contexts;
    }

    private String buildPlaylistName(List<String> genres, List<String> contexts, String fallbackMessage) {
        String base = null;
        if (genres != null && !genres.isEmpty()) {
            base = genres.get(0);
        }

        String context = null;
        if (contexts != null && !contexts.isEmpty()) {
            context = contexts.get(0);
        }

        if (StringUtils.hasText(base) && StringUtils.hasText(context)) {
            return base + " para " + context;
        }
        if (StringUtils.hasText(base)) {
            return base + " mix";
        }
        if (StringUtils.hasText(context)) {
            return "Musica para " + context;
        }

        if (StringUtils.hasText(fallbackMessage)) {
            String trimmed = fallbackMessage.trim();
            if (trimmed.length() > 60) {
                return trimmed.substring(0, 60).trim();
            }
            return trimmed;
        }

        return "Playlist personalizada";
    }

    private String buildDescription(String playlistName, String message) {
        String base = StringUtils.hasText(playlistName) ? playlistName : "tu playlist";
        return "Playlist creada por Spotify Tracker para " + base + ". Solicitud: " + message;
    }

    private List<GenreHint> genreHints() {
        return List.of(
            new GenreHint("rock alternativo", "Rock alternativo", List.of("alternative rock", "alt rock", "rock alternativo")),
            new GenreHint("synthwave", "Synthwave", List.of("synthwave", "retrowave", "outrun")),
            new GenreHint("indie", "Indie", List.of("indie", "indie rock")),
            new GenreHint("lofi", "Lo-fi", List.of("lofi", "lo-fi", "chillhop")),
            new GenreHint("hip hop", "Hip hop", List.of("hip hop", "hip-hop", "rap")),
            new GenreHint("rap", "Rap", List.of("rap", "hip hop")),
            new GenreHint("metal", "Metal", List.of("metal", "heavy metal")),
            new GenreHint("pop", "Pop", List.of("pop", "pop hits")),
            new GenreHint("reggaeton", "Reggaeton", List.of("reggaeton", "latin")),
            new GenreHint("electro", "Electronica", List.of("electronic", "electro")),
            new GenreHint("electronica", "Electronica", List.of("electronic", "electro")),
            new GenreHint("jazz", "Jazz", List.of("jazz", "smooth jazz")),
            new GenreHint("clasica", "Clasica", List.of("classical", "orchestra")),
            new GenreHint("blues", "Blues", List.of("blues", "classic blues")),
            new GenreHint("trap", "Trap", List.of("trap", "latin trap")),
            new GenreHint("house", "House", List.of("house", "deep house"))
        );
    }

    private List<ContextHint> contextHints() {
        return List.of(
            new ContextHint("entrenar", "entrenar", List.of("workout", "gym", "training")),
            new ContextHint("gimnasio", "entrenar", List.of("workout", "gym")),
            new ContextHint("gym", "entrenar", List.of("workout", "gym")),
            new ContextHint("estudiar", "estudiar", List.of("study", "focus")),
            new ContextHint("estudio", "estudiar", List.of("study", "focus")),
            new ContextHint("relajar", "relajar", List.of("relax", "chill")),
            new ContextHint("relajarse", "relajar", List.of("relax", "chill")),
            new ContextHint("noche", "noche", List.of("night", "late night")),
            new ContextHint("fiesta", "fiesta", List.of("party", "dance")),
            new ContextHint("conducir", "conducir", List.of("driving", "road trip")),
            new ContextHint("coche", "conducir", List.of("driving", "road trip")),
            new ContextHint("viajar", "viajar", List.of("travel", "road trip")),
            new ContextHint("trabajar", "trabajar", List.of("work", "focus")),
            new ContextHint("manana", "manana", List.of("morning", "energy"))
        );
    }

    private record GenreHint(String keyword, String displayName, List<String> queries) {
        String primaryQuery() {
            if (queries == null || queries.isEmpty()) {
                return displayName;
            }
            return queries.get(0);
        }
    }

    private record ContextHint(String keyword, String displayName, List<String> queries) {
        String primaryQuery() {
            if (queries == null || queries.isEmpty()) {
                return displayName;
            }
            return queries.get(0);
        }
    }
}