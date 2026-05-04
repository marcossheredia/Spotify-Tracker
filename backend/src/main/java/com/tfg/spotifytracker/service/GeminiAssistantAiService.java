package com.tfg.spotifytracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.spotifytracker.config.AssistantAiProperties;
import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "assistant.ai", name = "provider", havingValue = "gemini")
@RequiredArgsConstructor
public class GeminiAssistantAiService implements AssistantAiService {

    private static final int MIN_TRACK_LIMIT = 5;
    private static final int MAX_TRACK_LIMIT = 50;
    private static final int MIN_QUERY_COUNT = 3;
    private static final int MAX_QUERY_COUNT = 10;

    private final AssistantAiProperties assistantAiProperties;
    private final ObjectMapper objectMapper;

    @Qualifier("geminiWebClient")
    private final WebClient geminiWebClient;

    @Override
    public Optional<AssistantPlaylistPlanDTO> generatePlan(String message, Integer trackLimit, Boolean publicPlaylist) {
        AssistantAiProperties.Gemini gemini = assistantAiProperties.getGemini();
        if (gemini == null || !StringUtils.hasText(gemini.getApiKey())) {
            log.warn("Gemini AI no configurado: GEMINI_API_KEY ausente.");
            return Optional.empty();
        }

        if (!StringUtils.hasText(message)) {
            return Optional.empty();
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(message, trackLimit, publicPlaylist, gemini.getModel());
            Map<String, Object> response = geminiWebClient.post()
                .uri("/models/{model}:generateContent", gemini.getModel())
                .header("x-goog-api-key", gemini.getApiKey())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(gemini.getTimeoutSeconds()));

            String responseText = extractResponseText(response);
            if (!StringUtils.hasText(responseText)) {
                log.warn("Gemini AI respondio sin contenido util.");
                return Optional.empty();
            }

            AssistantPlaylistPlanDTO plan = parsePlan(responseText, message);
            if (plan == null) {
                return Optional.empty();
            }

            return Optional.of(plan);
        } catch (WebClientResponseException ex) {
            log.warn("Gemini AI fallo con status {}. Usando fallback.", ex.getStatusCode().value());
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Gemini AI fallo inesperado. Usando fallback. Motivo: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private Map<String, Object> buildRequestBody(String message,
                                                 Integer trackLimit,
                                                 Boolean publicPlaylist,
                                                 String model) {
        String prompt = buildPrompt(message, trackLimit, publicPlaylist, model);

        return Map.of(
            "system_instruction", Map.of(
                "parts", List.of(Map.of("text", buildSystemInstruction()))
            ),
            "contents", List.of(
                Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", prompt))
                )
            ),
            "generationConfig", Map.of(
                "temperature", 0.2,
                "maxOutputTokens", 512
            )
        );
    }

    private String buildSystemInstruction() {
        return String.join("\n",
            "Eres un planificador de playlists para una app llamada Spotify Tracker.",
            "Tu tarea es convertir el mensaje del usuario en un JSON estricto.",
            "No inventes canciones, IDs ni URIs de Spotify.",
            "No llames a ninguna API ni generes resultados finales.",
            "Devuelve solo JSON valido, sin markdown, sin bloques de codigo y sin texto extra.",
            "Usa un JSON con estas claves:",
            "originalMessage, playlistName, description, intent, language, contextTags, genres, searchQueries, trackLimit, publicPlaylist.",
            "searchQueries debe tener entre 3 y 10 elementos.",
            "trackLimit debe estar entre 5 y 50.",
            "Si el usuario es ambiguo, genera un plan razonable.",
            "Si el usuario pide algo no musical, usa intent='unknown' y genera queries genericas de musica.",
            "No respondas con comentarios ni explicaciones."
        );
    }

    private String buildPrompt(String message, Integer trackLimit, Boolean publicPlaylist, String model) {
        StringBuilder builder = new StringBuilder();
        builder.append("Mensaje del usuario: ").append(message).append("\n");
        if (trackLimit != null) {
            builder.append("trackLimit solicitado: ").append(trackLimit).append("\n");
        }
        if (publicPlaylist != null) {
            builder.append("publicPlaylist solicitado: ").append(publicPlaylist).append("\n");
        }
        builder.append("Responde solo JSON valido con las claves indicadas.\n");
        builder.append("Ejemplo de formato (no reutilices valores literalmente):\n");
        builder.append("{")
            .append("\"originalMessage\": \"...\",")
            .append("\"playlistName\": \"...\",")
            .append("\"description\": \"...\",")
            .append("\"intent\": \"playlist\",")
            .append("\"language\": \"es\",")
            .append("\"contextTags\": [\"...\"],")
            .append("\"genres\": [\"...\"],")
            .append("\"searchQueries\": [\"...\"],")
            .append("\"trackLimit\": 20,")
            .append("\"publicPlaylist\": false")
            .append("}");

        return builder.toString();
    }

    private String extractResponseText(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            return null;
        }

        try {
            JsonNode root = objectMapper.valueToTree(response);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return null;
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return null;
            }
            JsonNode textNode = parts.get(0).path("text");
            return textNode.isTextual() ? textNode.asText() : null;
        } catch (Exception ex) {
            log.warn("No se pudo interpretar la respuesta de Gemini.");
            return null;
        }
    }

    private AssistantPlaylistPlanDTO parsePlan(String responseText, String originalMessage) {
        String json = extractJson(responseText);
        if (!StringUtils.hasText(json)) {
            log.warn("Gemini AI respondio sin JSON valido.");
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(json);

            String playlistName = textValue(node, "playlistName", "name");
            String description = textValue(node, "description");
            String intent = textValue(node, "intent");
            String language = textValue(node, "language", "lang");
            String message = textValue(node, "originalMessage");

            List<String> contextTags = listValue(node, "contextTags", "contexts");
            List<String> genres = listValue(node, "genres");
            List<String> searchQueries = listValue(node, "searchQueries", "queries");

            Integer trackLimit = intValue(node, "trackLimit", "targetTrackCount");
            Boolean publicPlaylist = booleanValue(node, "publicPlaylist");

            if (!StringUtils.hasText(message)) {
                message = originalMessage;
            }

            trackLimit = normalizeTrackLimit(trackLimit);
            searchQueries = normalizeQueries(searchQueries);

            if (!hasAnyValue(playlistName, description, intent, language, contextTags, genres, searchQueries, trackLimit, publicPlaylist)) {
                return null;
            }

            return AssistantPlaylistPlanDTO.builder()
                .originalMessage(message)
                .playlistName(playlistName)
                .description(description)
                .intent(intent)
                .language(language)
                .contextTags(contextTags)
                .genres(genres)
                .searchQueries(searchQueries)
                .trackLimit(trackLimit)
                .publicPlaylist(publicPlaylist)
                .build();
        } catch (Exception ex) {
            log.warn("Gemini AI devolvio JSON invalido.");
            return null;
        }
    }

    private String extractJson(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }

        return text.substring(start, end + 1).trim();
    }

    private String textValue(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isTextual()) {
                String text = value.asText().trim();
                if (StringUtils.hasText(text)) {
                    return normalizeText(text);
                }
            }
        }
        return null;
    }

    private List<String> listValue(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isArray()) {
                List<String> items = new ArrayList<>();
                for (JsonNode item : value) {
                    if (item.isTextual()) {
                        String text = normalizeText(item.asText());
                        if (StringUtils.hasText(text)) {
                            items.add(text);
                        }
                    }
                }
                return items;
            }
        }
        return null;
    }

    private Integer intValue(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isNumber()) {
                return value.asInt();
            }
            if (value != null && value.isTextual()) {
                String text = value.asText();
                if (text.matches("\\d+")) {
                    return Integer.parseInt(text);
                }
            }
        }
        return null;
    }

    private Boolean booleanValue(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && value.isBoolean()) {
                return value.asBoolean();
            }
            if (value != null && value.isTextual()) {
                String text = value.asText().trim().toLowerCase();
                if ("true".equals(text) || "false".equals(text)) {
                    return Boolean.parseBoolean(text);
                }
            }
        }
        return null;
    }

    private Integer normalizeTrackLimit(Integer trackLimit) {
        if (trackLimit == null) {
            return null;
        }
        if (trackLimit < MIN_TRACK_LIMIT || trackLimit > MAX_TRACK_LIMIT) {
            return null;
        }
        return trackLimit;
    }

    private List<String> normalizeQueries(List<String> queries) {
        if (queries == null) {
            return null;
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String query : queries) {
            if (StringUtils.hasText(query)) {
                unique.add(normalizeText(query));
            }
        }

        if (unique.isEmpty()) {
            return null;
        }

        List<String> normalized = new ArrayList<>(unique);
        if (normalized.size() < MIN_QUERY_COUNT) {
            return null;
        }
        if (normalized.size() > MAX_QUERY_COUNT) {
            return normalized.subList(0, MAX_QUERY_COUNT);
        }
        return normalized;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private boolean hasAnyValue(String playlistName,
                                String description,
                                String intent,
                                String language,
                                List<String> contextTags,
                                List<String> genres,
                                List<String> searchQueries,
                                Integer trackLimit,
                                Boolean publicPlaylist) {
        return StringUtils.hasText(playlistName)
            || StringUtils.hasText(description)
            || StringUtils.hasText(intent)
            || StringUtils.hasText(language)
            || (contextTags != null && !contextTags.isEmpty())
            || (genres != null && !genres.isEmpty())
            || (searchQueries != null && !searchQueries.isEmpty())
            || trackLimit != null
            || publicPlaylist != null;
    }
}
