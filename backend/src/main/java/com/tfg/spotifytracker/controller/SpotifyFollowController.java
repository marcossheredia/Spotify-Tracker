package com.tfg.spotifytracker.controller;


import com.tfg.spotifytracker.dto.spotify.response.SpotifyActionResultDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.response.SpotifyPagedResponseDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifyFollowService;
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

@Tag(name = "Spotify Follow", description = "Artistas y playlists seguidas")
@Validated
@RestController
@RequestMapping("/api/spotify/follow")
@RequiredArgsConstructor
public class SpotifyFollowController {

    private final SpotifyFollowService spotifyFollowService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Obtener artistas seguidos")
    @GetMapping("/artists")
    public ResponseEntity<SpotifyPagedResponseDTO<SpotifyArtistDTO>> getArtists(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @RequestParam(required = false) String after
    ) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyFollowService.getFollowedArtists(accessToken, limit, after));
    }

    @Operation(summary = "Seguir artista")
    @PutMapping("/artists/{artistId}")
    public ResponseEntity<SpotifyActionResultDTO> followArtist(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String artistId
    ) {
        return ResponseEntity.ok(spotifyFollowService.followArtist(resolveAccessToken(usuario), artistId));
    }

    @Operation(summary = "Dejar de seguir artista")
    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<SpotifyActionResultDTO> unfollowArtist(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String artistId
    ) {
        return ResponseEntity.ok(spotifyFollowService.unfollowArtist(resolveAccessToken(usuario), artistId));
    }

    @Operation(summary = "Seguir playlist")
    @PutMapping("/playlists/{playlistId}")
    public ResponseEntity<SpotifyActionResultDTO> followPlaylist(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String playlistId
    ) {
        return ResponseEntity.ok(spotifyFollowService.followPlaylist(resolveAccessToken(usuario), playlistId));
    }

    @Operation(summary = "Dejar de seguir playlist")
    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<SpotifyActionResultDTO> unfollowPlaylist(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String playlistId
    ) {
        return ResponseEntity.ok(spotifyFollowService.unfollowPlaylist(resolveAccessToken(usuario), playlistId));
    }

    private String resolveAccessToken(Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        return spotifyTokenService.getValidAccessToken(usuario);
    }
}

