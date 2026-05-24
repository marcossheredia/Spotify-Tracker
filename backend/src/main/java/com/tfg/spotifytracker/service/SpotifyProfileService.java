package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.profile.response.SpotifyUserProfileDTO;
import com.tfg.spotifytracker.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifyProfileService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifyApiClient, SpotifyDtoMapper.
 */
public class SpotifyProfileService {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;

    /** Obtiene datos para esta parte del sistema. */

    public SpotifyUserProfileDTO getProfile(String accessToken, Usuario usuario) {
        Map<String, Object> profile = spotifyApiClient.getMap(accessToken, "/me");
        Map<String, Object> externalUrls = mapper.asMap(profile.get("external_urls"));
        Map<String, Object> followers = mapper.asMap(profile.get("followers"));

        String product = mapper.asString(profile.get("product"));
        boolean productKnown = product != null && !product.isBlank();
        boolean premium = "premium".equalsIgnoreCase(product);

        return SpotifyUserProfileDTO.builder()
            .spotifyId(mapper.asString(profile.getOrDefault("id", usuario.getSpotifyId())))
            .displayName(mapper.asString(profile.getOrDefault("display_name", usuario.getDisplayName())))
            .email(mapper.asString(profile.getOrDefault("email", usuario.getEmail())))
            .imageUrl(mapper.extractImageUrl(profile) != null ? mapper.extractImageUrl(profile) : usuario.getImageUrl())
            .country(mapper.asString(profile.getOrDefault("country", usuario.getCountry())))
            .product(product != null ? product : usuario.getProduct())
            .externalUrl(mapper.asString(externalUrls.get("spotify")))
            .followersTotal(mapper.asNullableInteger(followers.get("total")))
            .premiumCapabilitiesAvailable(premium)
            .capabilitiesNote(resolveCapabilitiesNote(productKnown, premium))
            .build();
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private String resolveCapabilitiesNote(boolean productKnown, boolean premium) {
        if (!productKnown) {
            return "Spotify ya no devuelve el tipo de cuenta; algunas acciones pueden estar restringidas.";
        }

        return premium
            ? "Tu cuenta permite controles de reproduccion remota en dispositivos compatibles."
            : "Algunas acciones del reproductor requieren Spotify Premium.";
    }
}
