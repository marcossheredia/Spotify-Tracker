package com.tfg.spotifytracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private String frontendBaseUrl = "http://localhost:3000";
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000L;
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000,http://localhost:5173";

        public List<String> allowedOriginsList() {
            return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        }
    }

    public String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(value.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }

            StringBuilder normalized = new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getHost());

            if (uri.getPort() != -1) {
                normalized.append(":").append(uri.getPort());
            }

            return normalized.toString();
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    public boolean isAllowedFrontendBaseUrl(String candidate, String backendHost) {
        String normalizedCandidate = normalizeBaseUrl(candidate);
        if (normalizedCandidate == null) {
            return false;
        }

        if (Objects.equals(normalizedCandidate, normalizeBaseUrl(frontendBaseUrl))) {
            return true;
        }

        boolean matchesConfiguredCorsOrigin = cors.allowedOriginsList().stream()
            .map(this::normalizeBaseUrl)
            .filter(Objects::nonNull)
            .anyMatch(normalizedCandidate::equals);

        if (matchesConfiguredCorsOrigin) {
            return true;
        }

        if (backendHost == null || backendHost.isBlank()) {
            return false;
        }

        URI candidateUri = URI.create(normalizedCandidate);
        return backendHost.equalsIgnoreCase(candidateUri.getHost());
    }

    public String resolveAllowedCorsOrigin(String origin, String backendHost) {
        String normalizedOrigin = normalizeBaseUrl(origin);
        if (normalizedOrigin == null) {
            return null;
        }

        return isAllowedFrontendBaseUrl(normalizedOrigin, backendHost)
            ? normalizedOrigin
            : null;
    }
}
