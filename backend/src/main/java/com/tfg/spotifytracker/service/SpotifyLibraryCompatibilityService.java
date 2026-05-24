package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifyLibraryCompatibilityService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifyApiClient.
 */
public class SpotifyLibraryCompatibilityService {

    private static final int LIBRARY_CONTAINS_BATCH_SIZE = 50;

    private final SpotifyApiClient spotifyApiClient;

    /** Guarda o actualiza datos en el sistema. */

    public void saveTrack(String accessToken, String trackId) {
        String uri = toSpotifyUri("track", trackId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.putNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.putNoContent(accessToken, "/me/tracks?ids=" + trackId, null);
                return;
            }
            throw ex;
        }
    }

    /** Elimina o desvincula datos según el caso. */

    public void removeTrack(String accessToken, String trackId) {
        String uri = toSpotifyUri("track", trackId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.deleteNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.deleteNoContent(accessToken, "/me/tracks?ids=" + trackId);
                return;
            }
            throw ex;
        }
    }

    /** Guarda o actualiza datos en el sistema. */

    public void saveAlbum(String accessToken, String albumId) {
        String uri = toSpotifyUri("album", albumId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.putNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.putNoContent(accessToken, "/me/albums?ids=" + albumId, null);
                return;
            }
            throw ex;
        }
    }

    /** Elimina o desvincula datos según el caso. */

    public void removeAlbum(String accessToken, String albumId) {
        String uri = toSpotifyUri("album", albumId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.deleteNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.deleteNoContent(accessToken, "/me/albums?ids=" + albumId);
                return;
            }
            throw ex;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public Map<String, Boolean> containsTracks(String accessToken, List<String> trackIds) {
        Map<String, Boolean> likedTracks = new HashMap<>();
        if (trackIds == null || trackIds.isEmpty()) {
            return likedTracks;
        }

        List<String> normalized = normalizeIds(trackIds);
        for (int index = 0; index < normalized.size(); index += LIBRARY_CONTAINS_BATCH_SIZE) {
            int end = Math.min(index + LIBRARY_CONTAINS_BATCH_SIZE, normalized.size());
            List<String> batch = normalized.subList(index, end);
            Map<String, Boolean> batchResult = containsTracksBatch(accessToken, batch);
            likedTracks.putAll(batchResult);
        }

        return likedTracks;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public void followArtist(String accessToken, String artistId) {
        String uri = toSpotifyUri("artist", artistId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.putNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.putNoContent(accessToken, "/me/following?type=artist&ids=" + artistId, null);
                return;
            }
            throw ex;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public void unfollowArtist(String accessToken, String artistId) {
        String uri = toSpotifyUri("artist", artistId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.deleteNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.deleteNoContent(accessToken, "/me/following?type=artist&ids=" + artistId);
                return;
            }
            throw ex;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public void followPlaylist(String accessToken, String playlistId) {
        String uri = toSpotifyUri("playlist", playlistId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.putNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.putNoContent(accessToken, "/playlists/" + playlistId + "/followers", Map.of("public", false));
                return;
            }
            throw ex;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public void unfollowPlaylist(String accessToken, String playlistId) {
        String uri = toSpotifyUri("playlist", playlistId);
        if (!StringUtils.hasText(uri)) {
            return;
        }

        try {
            spotifyApiClient.deleteNoContent(accessToken, "/me/library", Map.of("uris", List.of(uri)));
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                spotifyApiClient.deleteNoContent(accessToken, "/playlists/" + playlistId + "/followers");
                return;
            }
            throw ex;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Map<String, Boolean> containsTracksBatch(String accessToken, List<String> trackIds) {
        Map<String, Boolean> likedTracks = new HashMap<>();
        if (trackIds.isEmpty()) {
            return likedTracks;
        }

        List<String> uris = trackIds.stream()
            .map(id -> toSpotifyUri("track", id))
            .filter(StringUtils::hasText)
            .toList();

        if (uris.isEmpty()) {
            return likedTracks;
        }

        try {
            String uri = UriComponentsBuilder.fromPath("/me/library/contains")
                .queryParam("uris", String.join(",", uris))
                .build()
                .encode()
                .toUriString();

            List<Object> response = spotifyApiClient.getList(accessToken, uri);
            for (int index = 0; index < trackIds.size(); index++) {
                Object rawValue = index < response.size() ? response.get(index) : Boolean.FALSE;
                likedTracks.put(trackIds.get(index), Boolean.TRUE.equals(rawValue));
            }

            return likedTracks;
        } catch (SpotifyApiException ex) {
            if (shouldFallbackToLegacy(ex)) {
                return legacyContainsTracks(accessToken, trackIds);
            }
            throw ex;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Map<String, Boolean> legacyContainsTracks(String accessToken, List<String> trackIds) {
        Map<String, Boolean> likedTracks = new HashMap<>();
        String uri = "/me/tracks/contains?ids=" + String.join(",", trackIds);
        List<Object> response = spotifyApiClient.getList(accessToken, uri);

        for (int index = 0; index < trackIds.size(); index++) {
            Object rawValue = index < response.size() ? response.get(index) : Boolean.FALSE;
            likedTracks.put(trackIds.get(index), Boolean.TRUE.equals(rawValue));
        }

        return likedTracks;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private boolean shouldFallbackToLegacy(SpotifyApiException ex) {
        Integer status = ex.getStatusCode();
        if (status == null) {
            return false;
        }

        return status == 400 || status == 403 || status == 404 || status == 405;
    }

    /** Normaliza el valor de entrada para evitar errores. */

    private List<String> normalizeIds(List<String> ids) {
        Set<String> unique = new LinkedHashSet<>();
        for (String id : ids) {
            if (StringUtils.hasText(id)) {
                unique.add(id.trim());
            }
        }
        return new ArrayList<>(unique);
    }

    /** Transforma datos de un formato a otro. */

    private String toSpotifyUri(String type, String id) {
        if (!StringUtils.hasText(type) || !StringUtils.hasText(id)) {
            return null;
        }

        return "spotify:" + type + ":" + id.trim();
    }
}

