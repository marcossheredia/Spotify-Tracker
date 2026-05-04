package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.spotify.profile.response.SpotifyUserProfileDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifyProfileService;
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

@Tag(name = "Spotify Profile", description = "Perfil enriquecido del usuario en Spotify")
@RestController
@RequestMapping("/api/spotify/profile")
@RequiredArgsConstructor
public class SpotifyProfileController {

    private final SpotifyProfileService spotifyProfileService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Obtener perfil enriquecido del usuario")
    @GetMapping
    public ResponseEntity<SpotifyUserProfileDTO> getProfile(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        return ResponseEntity.ok(spotifyProfileService.getProfile(accessToken, usuario));
    }
}
