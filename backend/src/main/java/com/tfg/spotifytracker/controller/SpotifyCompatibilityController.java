package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyCompatibilityStatusDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifyCompatibilityService;
import com.tfg.spotifytracker.service.SpotifyTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Spotify Compatibility", description = "Diagnostico de compatibilidad con Spotify")
@RestController
@RequestMapping("/api/spotify/compatibility")
@RequiredArgsConstructor
public class SpotifyCompatibilityController {

    private final SpotifyCompatibilityService spotifyCompatibilityService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Diagnostico de compatibilidad con Spotify")
    @GetMapping("/status")
    public ResponseEntity<SpotifyCompatibilityStatusDTO> getStatus(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        return ResponseEntity.ok(spotifyCompatibilityService.getStatus(accessToken));
    }
}

