package com.tfg.spotifytracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifyApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: WebClient, ObjectMapper.
 */
public class SpotifyApiClient {

    @Qualifier("spotifyWebClient")
    private final WebClient spotifyWebClient;
    private final ObjectMapper objectMapper;

    /** Obtiene datos para esta parte del sistema. */

    public Map<String, Object> getMap(String accessToken, String uri) {
        return requestMap(accessToken, HttpMethod.GET, uri, null);
    }

    /** Obtiene datos para esta parte del sistema. */

    public List<Object> getList(String accessToken, String uri) {
        return requestList(accessToken, HttpMethod.GET, uri, null);
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public Map<String, Object> postMap(String accessToken, String uri, Object body) {
        return requestMap(accessToken, HttpMethod.POST, uri, body);
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public void postNoContent(String accessToken, String uri, Object body) {
        requestMap(accessToken, HttpMethod.POST, uri, body);
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public void putNoContent(String accessToken, String uri, Object body) {
        requestMap(accessToken, HttpMethod.PUT, uri, body);
    }

    /** Elimina o desvincula datos según el caso. */

    public void deleteNoContent(String accessToken, String uri) {
        requestMap(accessToken, HttpMethod.DELETE, uri, null);
    }

    /** Elimina o desvincula datos según el caso. */

    public void deleteNoContent(String accessToken, String uri, Object body) {
        requestMap(accessToken, HttpMethod.DELETE, uri, body);
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Map<String, Object> requestMap(String accessToken, HttpMethod method, String uri, Object body) {
        String safeAccessToken = Objects.requireNonNull(accessToken, "Spotify access token es obligatorio");
        HttpMethod safeMethod = Objects.requireNonNull(method, "HTTP method es obligatorio");
        String safeUri = Objects.requireNonNull(uri, "La URI de Spotify es obligatoria");

        try {
            WebClient.RequestBodySpec request = spotifyWebClient.method(safeMethod)
                .uri(safeUri)
                .headers(headers -> headers.setBearerAuth(safeAccessToken));

            Map<?, ?> response;
            if (body != null) {
                response = request.bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            } else {
                response = request
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            }

            return response != null ? asMap(response) : Collections.emptyMap();
        } catch (WebClientResponseException ex) {
            throw toSpotifyApiException(method, safeUri, ex);
        } catch (Exception ex) {
            throw new SpotifyApiException("Error inesperado al consultar Spotify", null, null, null, ex);
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private List<Object> requestList(String accessToken, HttpMethod method, String uri, Object body) {
        String safeAccessToken = Objects.requireNonNull(accessToken, "Spotify access token es obligatorio");
        HttpMethod safeMethod = Objects.requireNonNull(method, "HTTP method es obligatorio");
        String safeUri = Objects.requireNonNull(uri, "La URI de Spotify es obligatoria");

        try {
            WebClient.RequestBodySpec request = spotifyWebClient.method(safeMethod)
                .uri(safeUri)
                .headers(headers -> headers.setBearerAuth(safeAccessToken));

            List<?> response;
            if (body != null) {
                response = request.bodyValue(body)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            } else {
                response = request
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            }

            if (response == null) {
                return List.of();
            }

            return new ArrayList<>(response);
        } catch (WebClientResponseException ex) {
            throw toSpotifyApiException(method, safeUri, ex);
        } catch (Exception ex) {
            throw new SpotifyApiException("Error inesperado al consultar Spotify", null, null, null, ex);
        }
    }

    /** Transforma datos de un formato a otro. */

    private SpotifyApiException toSpotifyApiException(HttpMethod method, String uri, WebClientResponseException ex) {
        Integer retryAfterSeconds = resolveRetryAfterSeconds(ex);
        String spotifyErrorCode = resolveSpotifyErrorCode(ex);
        String responseBody = ex.getResponseBodyAsString();
        String spotifyMessage = resolveSpotifyErrorMessage(responseBody);
        String errorCategory = classifySpotifyError(ex, spotifyMessage);
        String message = String.format(
            "Error al consultar Spotify (%s %s, status=%s)%s",
            method.name(),
            uri,
            ex.getStatusCode().value(),
            StringUtils.hasText(spotifyMessage) ? ": " + spotifyMessage
                : StringUtils.hasText(responseBody) ? ": " + responseBody : ""
        );

        log.debug("Spotify API failure retryAfter={} code={} body={}", retryAfterSeconds, spotifyErrorCode, responseBody);

        return new SpotifyApiException(
            message,
            ex.getStatusCode().value(),
            retryAfterSeconds,
            spotifyErrorCode,
            errorCategory,
            ex
        );
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private Integer resolveRetryAfterSeconds(WebClientResponseException ex) {
        String retryAfter = ex.getHeaders().getFirst("Retry-After");
        if (retryAfter == null || retryAfter.isBlank() || !retryAfter.matches("\\d+")) {
            return null;
        }

        return Integer.parseInt(retryAfter);
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private String resolveSpotifyErrorCode(WebClientResponseException ex) {
        String retryReason = ex.getHeaders().getFirst("X-Spotify-Ratelimit-Reason");
        if (StringUtils.hasText(retryReason)) {
            return retryReason;
        }

        return null;
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private String resolveSpotifyErrorMessage(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(responseBody);
            JsonNode errorNode = node.path("error");
            if (!errorNode.isMissingNode()) {
                String message = errorNode.path("message").asText(null);
                if (StringUtils.hasText(message)) {
                    return message;
                }
            }

            String topLevelMessage = node.path("message").asText(null);
            if (StringUtils.hasText(topLevelMessage)) {
                return topLevelMessage;
            }
        } catch (Exception ex) {
            log.debug("No se pudo parsear el error de Spotify como JSON: {}", ex.getMessage());
        }

        return null;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private String classifySpotifyError(WebClientResponseException ex, String spotifyMessage) {
        int status = ex.getStatusCode().value();
        String message = spotifyMessage != null ? spotifyMessage.toLowerCase() : "";

        if (status == 429) {
            return "RATE_LIMITED";
        }
        if (status == 401) {
            return "AUTH_EXPIRED";
        }
        if (status == 404) {
            return "RESOURCE_NOT_AVAILABLE";
        }
        if (status == 403) {
            if (message.contains("premium")) {
                return "PREMIUM_REQUIRED";
            }
            if (message.contains("scope") || message.contains("permission") || message.contains("permissions")) {
                return "SCOPE_REQUIRED";
            }
            return "ENDPOINT_RESTRICTED";
        }

        return null;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }

        Map<String, Object> map = new HashMap<>();
        rawMap.forEach((key, mapValue) -> {
            if (key != null) {
                map.put(String.valueOf(key), mapValue);
            }
        });
        return map;
    }
}