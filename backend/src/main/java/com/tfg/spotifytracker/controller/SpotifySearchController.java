package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.spotify.search.response.SpotifySearchResultDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifySearchService;
import com.tfg.spotifytracker.service.SpotifyTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Spotify Search", description = "Buscador global unificado")
@Validated
@RestController
@RequestMapping("/api/spotify/search")
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifySearchController.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifySearchService, SpotifyTokenService.
 */
public class SpotifySearchController {

    private final SpotifySearchService spotifySearchService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Buscar tracks, artistas, albumes y playlists")
    @GetMapping
    public ResponseEntity<SpotifySearchResultDTO> search(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam String q,
        @RequestParam(defaultValue = "track,artist,album,playlist") String types,
        @RequestParam(defaultValue = "10") @Min(1) @Max(10) int limit,
        @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        return ResponseEntity.ok(spotifySearchService.search(accessToken, q, types, limit, offset));
    }
}
