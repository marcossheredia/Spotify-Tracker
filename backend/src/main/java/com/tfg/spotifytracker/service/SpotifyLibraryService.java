package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyActionResultDTO;
import com.tfg.spotifytracker.dto.spotify.response.SpotifyPagedResponseDTO;
import com.tfg.spotifytracker.dto.spotify.library.response.SpotifySavedAlbumDTO;
import com.tfg.spotifytracker.dto.spotify.library.response.SpotifySavedTrackDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifyLibraryService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifyApiClient, SpotifyDtoMapper, SpotifyLibraryCompatibilityService.
 */
public class SpotifyLibraryService {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;
    private final SpotifyLibraryCompatibilityService spotifyLibraryCompatibilityService;

    /** Obtiene datos para esta parte del sistema. */

    public SpotifyPagedResponseDTO<SpotifySavedTrackDTO> getSavedTracks(String accessToken, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);

        Map<String, Object> response = spotifyApiClient.getMap(
            accessToken,
            "/me/tracks?limit=" + safeLimit + "&offset=" + safeOffset
        );

        List<SpotifySavedTrackDTO> items = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(response)) {
            Map<String, Object> track = mapper.asMap(item.get("track"));
            if (track.isEmpty()) {
                continue;
            }

            items.add(SpotifySavedTrackDTO.builder()
                .id(mapper.asString(track.get("id")))
                .name(mapper.asString(track.get("name")))
                .imageUrl(mapper.extractImageUrl(mapper.asMap(track.get("album"))))
                .artists(mapper.extractNamedValues(track.get("artists"), "name"))
                .albumName(mapper.asString(mapper.asMap(track.get("album")).get("name")))
                .externalUrl(mapper.asString(mapper.asMap(track.get("external_urls")).get("spotify")))
                .durationMs(mapper.asNullableInteger(track.get("duration_ms")))
                .addedAt(mapper.asString(item.get("added_at")))
                .build());
        }

        return SpotifyPagedResponseDTO.<SpotifySavedTrackDTO>builder()
            .items(items)
            .limit(safeLimit)
            .offset(safeOffset)
            .total(mapper.asNullableInteger(response.get("total")))
            .hasNext(response.get("next") != null)
            .build();
    }

    /** Obtiene datos para esta parte del sistema. */

    public SpotifyPagedResponseDTO<SpotifySavedAlbumDTO> getSavedAlbums(String accessToken, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);

        Map<String, Object> response = spotifyApiClient.getMap(
            accessToken,
            "/me/albums?limit=" + safeLimit + "&offset=" + safeOffset
        );

        List<SpotifySavedAlbumDTO> items = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(response)) {
            Map<String, Object> album = mapper.asMap(item.get("album"));
            if (album.isEmpty()) {
                continue;
            }

            items.add(SpotifySavedAlbumDTO.builder()
                .id(mapper.asString(album.get("id")))
                .name(mapper.asString(album.get("name")))
                .imageUrl(mapper.extractImageUrl(album))
                .artists(mapper.extractNamedValues(album.get("artists"), "name"))
                .albumType(mapper.asString(album.get("album_type")))
                .totalTracks(mapper.asNullableInteger(album.get("total_tracks")))
                .releaseDate(mapper.asString(album.get("release_date")))
                .externalUrl(mapper.asString(mapper.asMap(album.get("external_urls")).get("spotify")))
                .addedAt(mapper.asString(item.get("added_at")))
                .build());
        }

        return SpotifyPagedResponseDTO.<SpotifySavedAlbumDTO>builder()
            .items(items)
            .limit(safeLimit)
            .offset(safeOffset)
            .total(mapper.asNullableInteger(response.get("total")))
            .hasNext(response.get("next") != null)
            .build();
    }

    /** Guarda o actualiza datos en el sistema. */

    public SpotifyActionResultDTO saveTrack(String accessToken, String trackId) {
        spotifyLibraryCompatibilityService.saveTrack(accessToken, trackId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("save-track")
            .message("Cancion guardada en Tu Biblioteca")
            .build();
    }

    /** Elimina o desvincula datos según el caso. */

    public SpotifyActionResultDTO removeTrack(String accessToken, String trackId) {
        spotifyLibraryCompatibilityService.removeTrack(accessToken, trackId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("remove-track")
            .message("Cancion eliminada de Tu Biblioteca")
            .build();
    }

    /** Guarda o actualiza datos en el sistema. */

    public SpotifyActionResultDTO saveAlbum(String accessToken, String albumId) {
        spotifyLibraryCompatibilityService.saveAlbum(accessToken, albumId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("save-album")
            .message("Album guardado en Tu Biblioteca")
            .build();
    }

    /** Elimina o desvincula datos según el caso. */

    public SpotifyActionResultDTO removeAlbum(String accessToken, String albumId) {
        spotifyLibraryCompatibilityService.removeAlbum(accessToken, albumId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("remove-album")
            .message("Album eliminado de Tu Biblioteca")
            .build();
    }
}
