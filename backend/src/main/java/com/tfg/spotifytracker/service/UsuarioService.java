package com.tfg.spotifytracker.service;


import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.ResourceNotFoundException;
import com.tfg.spotifytracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario saveOrUpdateUsuario(OAuth2User oauth2User,
                                       String accessToken,
                                       String refreshToken,
                                       Instant tokenExpiresAt) {
        Map<String, Object> attrs = oauth2User.getAttributes();

        String spotifyId    = (String) attrs.get("id");
        String displayName  = (String) attrs.getOrDefault("display_name", "");
        String email        = extractEmail(attrs);
        String imageUrl     = extractImageUrl(attrs);
        String country      = (String) attrs.getOrDefault("country", "");
        String product      = (String) attrs.getOrDefault("product", "");

        return usuarioRepository.findBySpotifyId(spotifyId)
            .map(existing -> {
                existing.setDisplayName(displayName);
                existing.setEmail(email);
                existing.setImageUrl(imageUrl);
                existing.setCountry(country);
                existing.setProduct(product);
                existing.setAccessToken(accessToken);
                if (refreshToken != null) existing.setRefreshToken(refreshToken);
                existing.setTokenExpiresAt(tokenExpiresAt);
                return usuarioRepository.save(existing);
            })
            .orElseGet(() -> {
                Usuario nuevo = Usuario.builder()
                    .spotifyId(spotifyId)
                    .displayName(displayName)
                    .email(email)
                    .imageUrl(imageUrl)
                    .country(country)
                    .product(product)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenExpiresAt(tokenExpiresAt)
                    .build();
                return usuarioRepository.save(nuevo);
            });
    }

    @Transactional(readOnly = true)
    public Usuario findById(UUID id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    @Transactional(readOnly = true)
    public Usuario findBySpotifyId(String spotifyId) {
        return usuarioRepository.findBySpotifyId(spotifyId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "spotifyId", spotifyId));
    }

    // ── helpers ──────────────────────────────────────

    private String extractEmail(Map<String, Object> attrs) {
        Object email = attrs.get("email");
        if (email instanceof String s) return s;
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractImageUrl(Map<String, Object> attrs) {
        try {
            var images = (java.util.List<Map<String, Object>>) attrs.get("images");
            if (images != null && !images.isEmpty()) {
                return (String) images.get(0).get("url");
            }
        } catch (Exception ignored) { }
        return null;
    }
}
