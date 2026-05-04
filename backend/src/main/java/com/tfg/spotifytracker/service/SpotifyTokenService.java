package com.tfg.spotifytracker.service;


import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyTokenService {

    private static final long EXPIRY_SAFETY_WINDOW_SECONDS = 30L;

    @Qualifier("spotifyAccountsWebClient")
    private final WebClient spotifyAccountsWebClient;
    private final UsuarioRepository usuarioRepository;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String spotifyClientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String spotifyClientSecret;

    @Transactional
    public String getValidAccessToken(Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        Instant now = Instant.now();
        Instant expiresAt = usuario.getTokenExpiresAt();

        if (expiresAt != null && expiresAt.isAfter(now.plusSeconds(EXPIRY_SAFETY_WINDOW_SECONDS))) {
            return usuario.getAccessToken();
        }

        return refreshAccessToken(usuario);
    }

    private String refreshAccessToken(Usuario usuario) {
        if (!StringUtils.hasText(usuario.getRefreshToken())) {
            throw new UnauthorizedException("No hay refresh token disponible para renovar la sesion de Spotify");
        }

        String safeRefreshToken = Objects.requireNonNull(usuario.getRefreshToken(), "Refresh token no puede ser null");
        String safeClientId = Objects.requireNonNull(spotifyClientId, "spotify client id no configurado");
        String safeClientSecret = Objects.requireNonNull(spotifyClientSecret, "spotify client secret no configurado");

        try {
            Map<?, ?> tokenResponse = spotifyAccountsWebClient.post()
                .uri("/api/token")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_FORM_URLENCODED, "MediaType no disponible"))
                .headers(headers -> headers.setBasicAuth(safeClientId, safeClientSecret))
                .body(BodyInserters
                    .fromFormData("grant_type", "refresh_token")
                    .with("refresh_token", safeRefreshToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (tokenResponse == null) {
                throw new SpotifyApiException("Spotify no devolvio datos al refrescar el token");
            }

            String refreshedAccessToken = asString(tokenResponse.get("access_token"));
            if (!StringUtils.hasText(refreshedAccessToken)) {
                throw new SpotifyApiException("Spotify no devolvio access_token en el refresh");
            }

            long expiresInSeconds = asLong(tokenResponse.get("expires_in"), 3600L);
            String refreshedRefreshToken = asString(tokenResponse.get("refresh_token"));

            usuario.setAccessToken(refreshedAccessToken);
            usuario.setTokenExpiresAt(Instant.now().plusSeconds(expiresInSeconds));
            if (StringUtils.hasText(refreshedRefreshToken)) {
                usuario.setRefreshToken(refreshedRefreshToken);
            }

            usuarioRepository.save(usuario);
            return refreshedAccessToken;
        } catch (WebClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            String responseBody = ex.getResponseBodyAsString();

            if (statusCode == 401) {
                throw new UnauthorizedException("Spotify rechazo el refresh token. Vuelve a iniciar sesion");
            }
            if (statusCode == 429) {
                Integer retryAfter = null;
                String retryAfterHeader = ex.getHeaders().getFirst("Retry-After");
                if (retryAfterHeader != null && retryAfterHeader.matches("\\d+")) {
                    retryAfter = Integer.parseInt(retryAfterHeader);
                }

                throw new SpotifyApiException(
                    "Spotify devolvio rate limit al refrescar token",
                    statusCode,
                    retryAfter,
                    ex.getHeaders().getFirst("X-Spotify-Ratelimit-Reason"),
                    ex
                );
            }

            throw new SpotifyApiException("Error al refrescar token de Spotify: " + responseBody, ex);
        } catch (Exception ex) {
            throw new SpotifyApiException("Error inesperado al refrescar token de Spotify", ex);
        }
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private long asLong(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text && text.matches("\\d+")) {
            return Long.parseLong(text);
        }

        return defaultValue;
    }
}

