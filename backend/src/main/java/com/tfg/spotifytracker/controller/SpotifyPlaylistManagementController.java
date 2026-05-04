package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyPagedResponseDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.request.SpotifyPlaylistAutomationRequestDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistAutomationResponseDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifyPlaylistManagementService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Spotify Playlist Management", description = "Inventario de playlists y automatizaciones")
@Validated
@RestController
@RequestMapping("/api/spotify/playlists")
@RequiredArgsConstructor
public class SpotifyPlaylistManagementController {

    private final SpotifyPlaylistManagementService spotifyPlaylistManagementService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Inventario completo de playlists")
    @GetMapping("/all")
    public ResponseEntity<SpotifyPagedResponseDTO<SpotifyPlaylistDTO>> getAllPlaylists(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyPlaylistManagementService.getAllPlaylists(accessToken, limit, offset));
    }

    @Operation(summary = "Crear playlist automatica desde top tracks")
    @PostMapping("/automations/top-tracks")
    public ResponseEntity<SpotifyPlaylistAutomationResponseDTO> createTopTracksPlaylist(
        @AuthenticationPrincipal Usuario usuario,
        @RequestBody SpotifyPlaylistAutomationRequestDTO request
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyPlaylistManagementService.createTopTracksPlaylist(accessToken, request));
    }

    private String resolveAccessToken(Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        return spotifyTokenService.getValidAccessToken(usuario);
    }
}

