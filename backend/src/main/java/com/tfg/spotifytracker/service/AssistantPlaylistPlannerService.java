package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AssistantPlaylistPlannerService {

    private static final int DEFAULT_TRACK_LIMIT = 30;
    private static final int MIN_TRACK_LIMIT = 5;
    private static final int MAX_TRACK_LIMIT = 50;
    private static final int MAX_INTERNAL_TRACK_LIMIT = 80;
    private static final int AVERAGE_TRACK_MINUTES = 4;

    public AssistantPlaylistPlanDTO buildPlan(String message, Integer trackLimit, Boolean publicPlaylist) {
        String safeMessage = normalizeMessage(message);
        String normalized = normalizeForMatching(safeMessage);

        String eventContext = detectEventContext(normalized);
        String audience = detectAudience(normalized);
        String genre = detectGenre(normalized);
        Integer targetDurationMinutes = detectDurationMinutes(normalized);
        YearRange yearRange = detectYearRange(normalized);
        boolean requireNonExplicit = shouldAvoidExplicit(normalized, audience, eventContext);
        List<String> avoidTerms = detectAvoidTerms(normalized);

        int effectiveTrackLimit = normalizeTrackLimit(trackLimit);
        if (targetDurationMinutes != null) {
            effectiveTrackLimit = Math.max(effectiveTrackLimit,
                Math.min(MAX_INTERNAL_TRACK_LIMIT, Math.max(20, targetDurationMinutes / AVERAGE_TRACK_MINUTES)));
        }

        List<String> queries = buildQueries(normalized, eventContext, audience, genre, yearRange);

        String playlistName = buildPlaylistName(eventContext, genre, yearRange, targetDurationMinutes);
        String description = "Playlist creada por Spotify Tracker";

        List<String> contextTags = new ArrayList<>();
        if (StringUtils.hasText(eventContext)) {
            contextTags.add(eventContext);
        }
        if (StringUtils.hasText(audience)) {
            contextTags.add(audience);
        }

        return AssistantPlaylistPlanDTO.builder()
            .originalMessage(safeMessage)
            .playlistName(playlistName)
            .description(description)
            .intent("playlist")
            .language(detectLanguage(normalized))
            .eventContext(eventContext)
            .targetAudience(audience)
            .primaryGenre(genre)
            .yearStart(yearRange.start())
            .yearEnd(yearRange.end())
            .targetDurationMinutes(targetDurationMinutes)
            .avoidExplicit(requireNonExplicit)
            .contextTags(contextTags)
            .genres(genre != null ? List.of(genre) : List.of())
            .negativeConstraints(avoidTerms)
            .searchQueries(queries)
            .trackLimit(effectiveTrackLimit)
            .publicPlaylist(Boolean.TRUE.equals(publicPlaylist))
            .build();
    }

    private List<String> buildQueries(String normalized, String eventContext, String audience, String genre, YearRange yearRange) {
        Set<String> queries = new LinkedHashSet<>();

        if ("wedding".equals(eventContext)) {
            queries.add("romantic wedding songs");
            queries.add("wedding love songs");
            queries.add("classic love songs");
            queries.add("elegant romantic ballads");
            queries.add("spanish romantic songs");
            queries.add("wedding dinner music");
        }

        if ("classroom".equals(eventContext) || "kids".equals(audience)) {
            queries.add("calm classroom music");
            queries.add("relaxing kids music");
            queries.add("instrumental calm music");
            queries.add("peaceful children music");
        }

        if ("study".equals(eventContext)) {
            queries.add("study music");
            queries.add("calm focus music");
            queries.add("instrumental study");
            queries.add("lo-fi study music");
        }

        if ("workout".equals(eventContext)) {
            queries.add("workout rock");
            queries.add("gym motivation music");
            queries.add("energetic workout songs");
            queries.add("training music");
        }

        if ("party".equals(eventContext)) {
            queries.add("party classics");
            queries.add("happy party songs");
            queries.add("dance party hits");
            queries.add("pop party songs");
        }

        if ("rock".equals(genre) && yearRange.is(1970, 1979)) {
            queries.add("classic rock 70s");
            queries.add("70s rock classics");
            queries.add("rock classics");
        }

        if ("disco".equals(genre) && yearRange.is(1980, 1989)) {
            queries.add("disco 80s classics");
            queries.add("80s dance classics");
            queries.add("funk disco 80s");
        }

        if ("rock".equals(genre) && queries.isEmpty()) {
            queries.add("rock hits");
            queries.add("rock classics");
            queries.add("classic rock");
            queries.add("rock songs");
        }

        if ("pop".equals(genre) && queries.isEmpty()) {
            queries.add("pop hits");
            queries.add("pop songs");
            queries.add("popular pop");
        }

        if (queries.isEmpty() && StringUtils.hasText(genre)) {
            queries.add("classic " + genre);
            queries.add(genre + " hits");
        }

        if (queries.isEmpty()) {
            queries.add("popular songs");
            queries.add("top hits");
            queries.add("calm music");
        }

        return new ArrayList<>(queries);
    }

    private int normalizeTrackLimit(Integer trackLimit) {
        int value = trackLimit == null ? DEFAULT_TRACK_LIMIT : trackLimit;
        return Math.max(MIN_TRACK_LIMIT, Math.min(MAX_TRACK_LIMIT, value));
    }

    private String detectLanguage(String normalized) {
        if (!StringUtils.hasText(normalized)) {
            return "es";
        }
        return normalized.contains("quiero") || normalized.contains("musica") ? "es" : "en";
    }

    private String detectEventContext(String normalized) {
        if (normalized.contains("boda")) return "wedding";
        if (normalized.contains("clase")) return "classroom";
        if (normalized.contains("fiesta")) return "party";
        if (normalized.contains("estudi")) return "study";
        if (normalized.contains("dormir")) return "sleep";
        if (normalized.contains("conduc") || normalized.contains("coche")) return "driving";
        if (normalized.contains("cena")) return "dinner";
        if (normalized.contains("entren") || normalized.contains("gimnas")) return "workout";
        return null;
    }

    private String detectAudience(String normalized) {
        if (normalized.contains("nino") || normalized.contains("nina") || normalized.contains("kids") || normalized.contains("children")) {
            return "kids";
        }
        return "general";
    }

    private String detectGenre(String normalized) {
        if (normalized.contains("rock")) return "rock";
        if (normalized.contains("disco")) return "disco";
        if (normalized.contains("jazz")) return "jazz";
        if (normalized.contains("metal")) return "metal";
        if (normalized.contains("reggaeton")) return "reggaeton";
        if (normalized.contains("instrumental")) return "instrumental";
        return "pop";
    }

    private Integer detectDurationMinutes(String normalized) {
        Matcher hours = Pattern.compile("(\\d{1,2})\\s*(hora|horas|h)").matcher(normalized);
        if (hours.find()) {
            return Integer.parseInt(hours.group(1)) * 60;
        }
        Matcher minutes = Pattern.compile("(\\d{2,3})\\s*(min|mins|minutos|minutes)").matcher(normalized);
        if (minutes.find()) {
            return Integer.parseInt(minutes.group(1));
        }
        return null;
    }

    private YearRange detectYearRange(String normalized) {
        if (normalized.contains("70") && (normalized.contains("anos") || normalized.contains("a\u00f1os") || normalized.contains("70s"))) {
            return new YearRange(1970, 1979);
        }
        if (normalized.contains("80") && (normalized.contains("anos") || normalized.contains("a\u00f1os") || normalized.contains("80s"))) {
            return new YearRange(1980, 1989);
        }
        if (normalized.contains("90") && (normalized.contains("anos") || normalized.contains("a\u00f1os") || normalized.contains("90s"))) {
            return new YearRange(1990, 1999);
        }
        if (normalized.contains("2000")) {
            return new YearRange(2000, 2009);
        }
        return new YearRange(null, null);
    }

    private boolean shouldAvoidExplicit(String normalized, String audience, String eventContext) {
        return "kids".equals(audience)
            || "classroom".equals(eventContext)
            || normalized.contains("sin explicit")
            || normalized.contains("sin explicitas");
    }

    private List<String> detectAvoidTerms(String normalized) {
        List<String> avoid = new ArrayList<>();
        if (normalized.contains("sin remix") || normalized.contains("remixes")) avoid.add("remix");
        if (normalized.contains("sin live") || normalized.contains("en vivo")) avoid.add("live");
        if (normalized.contains("sin cover")) avoid.add("cover");
        if (normalized.contains("tribute")) avoid.add("tribute");
        if (normalized.contains("karaoke")) avoid.add("karaoke");
        if (normalized.contains("sin hip hop") || normalized.contains("nada de hip hop")) avoid.add("hip hop");
        if (normalized.contains("pop punk")) avoid.add("pop punk");
        if (normalized.contains("nada moderno") || normalized.contains("sin moderno")) avoid.add("modern");
        return avoid;
    }

    private String buildPlaylistName(String eventContext, String genre, YearRange yearRange, Integer durationMinutes) {
        StringBuilder name = new StringBuilder();
        if ("wedding".equals(eventContext)) {
            name.append("Boda Romantica");
        } else if (StringUtils.hasText(genre)) {
            name.append(Character.toUpperCase(genre.charAt(0))).append(genre.substring(1));
        } else {
            name.append("Playlist personalizada");
        }

        if (yearRange.start() != null && yearRange.end() != null) {
            name.append(" ").append(yearRange.start()).append("-").append(yearRange.end());
        }
        if (durationMinutes != null && durationMinutes >= 120) {
            name.append(" - ").append(durationMinutes / 60).append("h");
        }
        return name.toString();
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

    private record YearRange(Integer start, Integer end) {
        boolean is(int targetStart, int targetEnd) {
            return start != null && end != null && start == targetStart && end == targetEnd;
        }
    }
}
