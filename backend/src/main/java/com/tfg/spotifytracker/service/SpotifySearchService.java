package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.common.SpotifyAlbumDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.dto.spotify.search.response.SpotifySearchResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifySearchService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifyApiClient, SpotifyDtoMapper.
 */
public class SpotifySearchService {

    private static final int MAX_LIMIT = 10;

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public SpotifySearchResultDTO search(String accessToken, String query, String types, int limit, int offset) {
        String safeQuery = query == null ? "" : query.trim();
        String safeTypes = resolveTypes(types);
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        int safeOffset = Math.max(0, offset);

        if (!StringUtils.hasText(safeQuery) || safeQuery.length() < 3 || !StringUtils.hasText(safeTypes)) {
            return emptyResult(safeQuery);
        }

        Map<String, Object> response = spotifyApiClient.getMap(
            accessToken,
            buildSearchUri(safeQuery, safeTypes, safeLimit, safeOffset)
        );

        return SpotifySearchResultDTO.builder()
            .query(safeQuery)
            .tracks(extractTracks(response))
            .artists(extractArtists(response))
            .albums(extractAlbums(response))
            .playlists(extractPlaylists(response))
            .build();
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private SpotifySearchResultDTO emptyResult(String query) {
        return SpotifySearchResultDTO.builder()
            .query(query)
            .tracks(List.of())
            .artists(List.of())
            .albums(List.of())
            .playlists(List.of())
            .build();
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private String resolveTypes(String rawTypes) {
        if (!StringUtils.hasText(rawTypes)) {
            return "track,artist,album,playlist";
        }

        List<String> allowedTypes = List.of("track", "artist", "album", "playlist");
        List<String> selectedTypes = new ArrayList<>();
        for (String type : rawTypes.split(",")) {
            String normalized = type == null ? "" : type.trim().toLowerCase();
            if (allowedTypes.contains(normalized) && !selectedTypes.contains(normalized)) {
                selectedTypes.add(normalized);
            }
        }

        return selectedTypes.isEmpty() ? "track,artist,album,playlist" : String.join(",", selectedTypes);
    }

    /** Construye una respuesta o estructura intermedia. */

    private String buildSearchUri(String query, String types, int limit, int offset) {
        return UriComponentsBuilder.fromPath("/search")
            .queryParam("q", query)
            .queryParam("type", types)
            .queryParam("limit", limit)
            .queryParam("offset", offset)
            .queryParam("market", "from_token")
            .build()
            .encode()
            .toUriString();
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private List<SpotifyTrackDTO> extractTracks(Map<String, Object> response) {
        Map<String, Object> tracksPage = mapper.asMap(response.get("tracks"));
        List<SpotifyTrackDTO> tracks = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(tracksPage)) {
            tracks.add(mapper.toTrack(item));
        }
        return tracks;
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private List<SpotifyArtistDTO> extractArtists(Map<String, Object> response) {
        Map<String, Object> artistsPage = mapper.asMap(response.get("artists"));
        List<SpotifyArtistDTO> artists = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(artistsPage)) {
            artists.add(mapper.toArtist(item));
        }
        return artists;
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private List<SpotifyAlbumDTO> extractAlbums(Map<String, Object> response) {
        Map<String, Object> albumsPage = mapper.asMap(response.get("albums"));
        List<SpotifyAlbumDTO> albums = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(albumsPage)) {
            albums.add(mapper.toAlbum(item));
        }
        return albums;
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private List<SpotifyPlaylistDTO> extractPlaylists(Map<String, Object> response) {
        Map<String, Object> playlistsPage = mapper.asMap(response.get("playlists"));
        List<SpotifyPlaylistDTO> playlists = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(playlistsPage)) {
            playlists.add(mapper.toPlaylist(item));
        }
        return playlists;
    }
}
