package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.playback.response.RecentPlaybackSyncResponseDTO;
import com.tfg.spotifytracker.dto.playback.response.PlaytimeHistoryResponseDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyNowPlayingDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDetailDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.RecentPlaybackSyncService;
import com.tfg.spotifytracker.service.SpotifyService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.Instant;

@Tag(name = "Spotify", description = "Consultas de datos del usuario en Spotify")
@Validated
@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final RecentPlaybackSyncService recentPlaybackSyncService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Obtener las últimas playlists reproducidas")
    @GetMapping("/playlists/recent")
    public ResponseEntity<List<SpotifyPlaylistDTO>> getRecentPlaylists(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(defaultValue = "5") @Min(1) @Max(500) int limit
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        List<SpotifyPlaylistDTO> playlists = spotifyService.getRecentlyPlayedPlaylists(accessToken, limit);
        return ResponseEntity.ok(playlists);
    }

    @Operation(summary = "Obtener detalle de una playlist con tracks y favoritos")
    @GetMapping("/playlists/{playlistId}/detail")
    public ResponseEntity<SpotifyPlaylistDetailDTO> getPlaylistDetail(
        @AuthenticationPrincipal Usuario usuario,
        @PathVariable String playlistId,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        SpotifyPlaylistDetailDTO playlistDetail = spotifyService.getPlaylistDetail(accessToken, playlistId, limit);
        return ResponseEntity.ok(playlistDetail);
    }

    @Operation(summary = "Obtener top canciones del usuario")
    @GetMapping("/top/tracks")
    public ResponseEntity<List<SpotifyTrackDTO>> getTopTracks(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit,
        @RequestParam(defaultValue = "short_term") String timeRange
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        List<SpotifyTrackDTO> tracks = spotifyService.getTopTracks(accessToken, limit, timeRange);
        return ResponseEntity.ok(tracks);
    }

    @Operation(summary = "Obtener top artistas del usuario")
    @GetMapping("/top/artists")
    public ResponseEntity<List<SpotifyArtistDTO>> getTopArtists(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit,
        @RequestParam(defaultValue = "short_term") String timeRange
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        List<SpotifyArtistDTO> artists = spotifyService.getTopArtists(accessToken, limit, timeRange);
        return ResponseEntity.ok(artists);
    }

    @Operation(summary = "Obtener la cancion en reproduccion actual")
    @GetMapping("/player/current")
    public ResponseEntity<SpotifyNowPlayingDTO> getCurrentlyPlayingTrack(
        @AuthenticationPrincipal Usuario usuario
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        SpotifyNowPlayingDTO currentTrack = spotifyService.getCurrentlyPlayingTrack(accessToken);

        if (currentTrack == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(currentTrack);
    }

    @Operation(summary = "Sincronizar reproducciones recientes y actualizar playtime")
    @PostMapping("/sync/recent-playtime")
    public ResponseEntity<RecentPlaybackSyncResponseDTO> syncRecentPlaytime(
        @AuthenticationPrincipal Usuario usuario
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        return ResponseEntity.ok(recentPlaybackSyncService.syncRecentPlaytime(usuario));
    }

    @Operation(summary = "Obtener estadisticas acumuladas de playtime")
    @GetMapping("/stats/playtime")
    public ResponseEntity<RecentPlaybackSyncResponseDTO> getPlaytimeStats(
        @AuthenticationPrincipal Usuario usuario
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        return ResponseEntity.ok(recentPlaybackSyncService.getPlaytimeStats(usuario));
    }

    @Operation(summary = "Obtener historico de playtime")
    @GetMapping("/stats/playtime/history")
    public ResponseEntity<PlaytimeHistoryResponseDTO> getPlaytimeHistory(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) String granularity
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        return ResponseEntity.ok(recentPlaybackSyncService.getPlaytimeHistory(usuario, from, to, granularity));
    }
}
