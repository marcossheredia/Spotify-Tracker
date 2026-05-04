package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyActionResultDTO;
import com.tfg.spotifytracker.dto.spotify.response.SpotifyPagedResponseDTO;
import com.tfg.spotifytracker.dto.spotify.library.response.SpotifySavedAlbumDTO;
import com.tfg.spotifytracker.dto.spotify.library.response.SpotifySavedTrackDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifyLibraryService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Spotify Library", description = "Lectura y acciones sobre la biblioteca de Spotify")
@Validated
@RestController
@RequestMapping("/api/spotify/library")
@RequiredArgsConstructor
public class SpotifyLibraryController {

    private final SpotifyLibraryService spotifyLibraryService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Obtener canciones guardadas")
    @GetMapping("/tracks")
    public ResponseEntity<SpotifyPagedResponseDTO<SpotifySavedTrackDTO>> getSavedTracks(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyLibraryService.getSavedTracks(accessToken, limit, offset));
    }

    @Operation(summary = "Obtener albumes guardados")
    @GetMapping("/albums")
    public ResponseEntity<SpotifyPagedResponseDTO<SpotifySavedAlbumDTO>> getSavedAlbums(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyLibraryService.getSavedAlbums(accessToken, limit, offset));
    }

    @Operation(summary = "Guardar cancion")
    @PutMapping("/tracks/{trackId}")
    public ResponseEntity<SpotifyActionResultDTO> saveTrack(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String trackId
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyLibraryService.saveTrack(accessToken, trackId));
    }

    @Operation(summary = "Quitar cancion guardada")
    @DeleteMapping("/tracks/{trackId}")
    public ResponseEntity<SpotifyActionResultDTO> removeTrack(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String trackId
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyLibraryService.removeTrack(accessToken, trackId));
    }

    @Operation(summary = "Guardar album")
    @PutMapping("/albums/{albumId}")
    public ResponseEntity<SpotifyActionResultDTO> saveAlbum(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String albumId
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyLibraryService.saveAlbum(accessToken, albumId));
    }

    @Operation(summary = "Quitar album guardado")
    @DeleteMapping("/albums/{albumId}")
    public ResponseEntity<SpotifyActionResultDTO> removeAlbum(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String albumId
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyLibraryService.removeAlbum(accessToken, albumId));
    }

    private String resolveAccessToken(Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        return spotifyTokenService.getValidAccessToken(usuario);
    }
}

