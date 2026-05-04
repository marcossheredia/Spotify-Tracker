package com.tfg.spotifytracker.service;


import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyNowPlayingDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDetailDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistTrackDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Servicio para hacer llamadas a la API de Spotify.
 * Amplía fácilmente añadiendo nuevos métodos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyService {

    private static final int MAX_PLAYLISTS_LIST_LIMIT = 500;
    private static final int MAX_RECENT_PLAYLISTS_LIKED_PROBE = 12;
    private static final int MAX_RECENT_TRACKS_TO_SCAN = 50;
    private static final int MAX_TRACKS_FOR_LIKED_PROBE = 25;
    private static final int MAX_PLAYLIST_DETAIL_TRACKS = 100;
    private static final String PLAYLIST_CONTEXT_TYPE = "playlist";

    @Qualifier("spotifyWebClient")
    private final WebClient spotifyWebClient;
    private final SpotifyLibraryCompatibilityService spotifyLibraryCompatibilityService;

    @Retryable(maxAttempts = 2)
    public Map<String, Object> getCurrentUserProfile(String accessToken) {
        return getSpotifyResource(accessToken, "/me");
    }

    @Retryable(maxAttempts = 2)
    public List<SpotifyPlaylistDTO> getRecentlyPlayedPlaylists(String accessToken, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_PLAYLISTS_LIST_LIMIT));
        LinkedHashMap<String, String> playlistLastPlayedById = collectRecentPlaylistIds(accessToken, safeLimit);
        Map<String, Object> currentUserProfile = getCurrentUserProfile(accessToken);
        String currentUserId = asString(currentUserProfile.get("id"));

        List<SpotifyPlaylistDTO> playlists = new ArrayList<>();
        int recentProbeCount = 0;

        for (Map.Entry<String, String> entry : playlistLastPlayedById.entrySet()) {
            try {
                Map<String, Object> playlistDetails = getSpotifyResource(accessToken, "/playlists/" + entry.getKey());
                boolean shouldProbeLikedTracks = recentProbeCount < MAX_RECENT_PLAYLISTS_LIKED_PROBE;
                playlists.add(
                    mapToPlaylistDto(
                        accessToken,
                        playlistDetails,
                        entry.getValue(),
                        currentUserId,
                        shouldProbeLikedTracks
                    )
                );
                recentProbeCount++;
            } catch (SpotifyApiException ex) {
                log.warn("No se pudo recuperar la playlist {}: {}", entry.getKey(), ex.getMessage());
            }
        }

        if (playlists.size() < safeLimit) {
            fillWithUserPlaylists(accessToken, safeLimit, playlistLastPlayedById.keySet(), playlists, currentUserId);
        }

        return playlists;
    }

    @Retryable(maxAttempts = 2)
    public SpotifyPlaylistDetailDTO getPlaylistDetail(String accessToken, String playlistId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_PLAYLIST_DETAIL_TRACKS));
        String safePlaylistId = playlistId != null ? playlistId.trim() : null;

        if (!StringUtils.hasText(safePlaylistId)) {
            return buildUnavailablePlaylistDetail(null, "No se pudo identificar la playlist solicitada.");
        }

        Map<String, Object> currentUserProfile = getCurrentUserProfile(accessToken);
        String currentUserId = asString(currentUserProfile.get("id"));

        Map<String, Object> playlist;
        try {
            playlist = getSpotifyResource(accessToken, "/playlists/" + safePlaylistId);
        } catch (SpotifyApiException ex) {
            if (isAccessRestricted(ex)) {
                return buildUnavailablePlaylistDetail(safePlaylistId, resolvePlaylistUnavailableReason(ex));
            }
            throw ex;
        }

        Map<String, Object> externalUrls = asMap(playlist.get("external_urls"));
        SpotifyPlaylistDetailDTO.SpotifyPlaylistDetailDTOBuilder detailBuilder = SpotifyPlaylistDetailDTO.builder()
            .id(asString(playlist.get("id")))
            .name(asString(playlist.get("name")))
            .imageUrl(extractImageUrl(playlist))
            .tracksTotal(extractPlaylistTracksTotal(playlist))
            .ownerName(extractPlaylistOwnerName(playlist))
            .externalUrl(asString(externalUrls.get("spotify")))
            .ownPlaylist(isOwnPlaylist(playlist, currentUserId))
            .collaborative(Boolean.TRUE.equals(asBoolean(playlist.get("collaborative"))))
            .canLoadTracks(true);

        try {
            List<SpotifyPlaylistTrackDTO> tracks = getPlaylistTracksWithLikedStatus(accessToken, safePlaylistId, safeLimit);
            boolean hasLikedTracks = tracks.stream().anyMatch(track -> Boolean.TRUE.equals(track.getLiked()));

            return detailBuilder
                .hasLikedTracks(hasLikedTracks)
                .tracks(tracks)
                .build();
        } catch (SpotifyApiException ex) {
            if (isAccessRestricted(ex)) {
                return detailBuilder
                    .canLoadTracks(false)
                    .hasLikedTracks(false)
                    .tracks(List.of())
                    .unavailableReason(resolvePlaylistUnavailableReason(ex))
                    .build();
            }
            throw ex;
        }
    }

    @Retryable(maxAttempts = 2)
    public Map<String, Object> getRecentlyPlayed(String accessToken, Long after, Integer limit) {
        return getRecentlyPlayed(accessToken, after, null, limit);
    }

    @Retryable(maxAttempts = 2)
    public Map<String, Object> getRecentlyPlayed(String accessToken, Long after, Long before, Integer limit) {
        int safeLimit = Math.max(1, Math.min(limit != null ? limit : 20, 50));

        StringBuilder uriBuilder = new StringBuilder("/me/player/recently-played?limit=")
            .append(safeLimit);

        if (after != null && after > 0) {
            uriBuilder.append("&after=").append(after);
        } else if (before != null && before > 0) {
            uriBuilder.append("&before=").append(before);
        }

        return getSpotifyResource(accessToken, uriBuilder.toString());
    }

    @Retryable(maxAttempts = 2)
    public List<SpotifyTrackDTO> getTopTracks(String accessToken, int limit, String timeRange) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        String safeTimeRange = SpotifyTimeRange.fromQuery(timeRange).getApiValue();
        Map<String, Object> topTracks = getSpotifyResource(
            accessToken,
            "/me/top/tracks?limit=" + safeLimit + "&time_range=" + safeTimeRange
        );

        List<SpotifyTrackDTO> tracks = new ArrayList<>();
        for (Map<String, Object> item : extractItems(topTracks)) {
            tracks.add(mapToTrackDto(item));
        }
        return tracks;
    }

    @Retryable(maxAttempts = 2)
    public List<SpotifyArtistDTO> getTopArtists(String accessToken, int limit, String timeRange) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        String safeTimeRange = SpotifyTimeRange.fromQuery(timeRange).getApiValue();
        Map<String, Object> topArtists = getSpotifyResource(
            accessToken,
            "/me/top/artists?limit=" + safeLimit + "&time_range=" + safeTimeRange
        );

        List<SpotifyArtistDTO> artists = new ArrayList<>();
        for (Map<String, Object> item : extractItems(topArtists)) {
            artists.add(mapToArtistDto(enrichArtistWithDetails(accessToken, item)));
        }
        return artists;
    }

    @Retryable(maxAttempts = 2)
    public SpotifyNowPlayingDTO getCurrentlyPlayingTrack(String accessToken) {
        Map<String, Object> currentlyPlaying = getSpotifyResource(accessToken, "/me/player/currently-playing");
        Map<String, Object> item = asMap(currentlyPlaying.get("item"));
        if (item.isEmpty()) {
            return null;
        }

        Map<String, Object> album = asMap(item.get("album"));
        Map<String, Object> externalUrls = asMap(item.get("external_urls"));

        return SpotifyNowPlayingDTO.builder()
            .id(asString(item.get("id")))
            .name(asString(item.get("name")))
            .imageUrl(extractImageUrl(album))
            .artists(extractNamedValues(item.get("artists"), "name"))
            .albumName(asString(album.get("name")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .isPlaying(asBoolean(currentlyPlaying.get("is_playing")))
            .progressMs(asNullableInteger(currentlyPlaying.get("progress_ms")))
            .durationMs(asNullableInteger(item.get("duration_ms")))
            .build();
    }

    private LinkedHashMap<String, String> collectRecentPlaylistIds(String accessToken, int limit) {
        Map<String, Object> recentlyPlayed = getRecentlyPlayed(accessToken, null, MAX_RECENT_TRACKS_TO_SCAN);

        LinkedHashMap<String, String> playlistLastPlayedById = new LinkedHashMap<>();

        for (Map<String, Object> item : extractItems(recentlyPlayed)) {
            Map<String, Object> context = asMap(item.get("context"));
            String playlistId = extractPlaylistId(context);
            if (!StringUtils.hasText(playlistId)) {
                continue;
            }

            playlistLastPlayedById.putIfAbsent(playlistId, asString(item.get("played_at")));
            if (playlistLastPlayedById.size() >= limit) {
                break;
            }
        }

        return playlistLastPlayedById;
    }

    private void fillWithUserPlaylists(String accessToken,
                                       int limit,
                                       Set<String> alreadyIncludedIds,
                                       List<SpotifyPlaylistDTO> target,
                                       String currentUserId) {
        final int pageSize = 50;
        int offset = 0;
        Set<String> includedIds = new HashSet<>(alreadyIncludedIds);

        while (target.size() < limit) {
            Map<String, Object> userPlaylists = getSpotifyResource(
                accessToken,
                "/me/playlists?limit=" + pageSize + "&offset=" + offset
            );

            List<Map<String, Object>> pageItems = extractItems(userPlaylists);
            if (pageItems.isEmpty()) {
                return;
            }

            for (Map<String, Object> item : pageItems) {
                String playlistId = asString(item.get("id"));
                if (!StringUtils.hasText(playlistId) || includedIds.contains(playlistId)) {
                    continue;
                }

                target.add(mapToPlaylistDto(accessToken, item, null, currentUserId, false));
                includedIds.add(playlistId);
                if (target.size() >= limit) {
                    return;
                }
            }

            offset += pageItems.size();
            Integer total = asNullableInteger(userPlaylists.get("total"));
            if (pageItems.size() < pageSize || (total != null && offset >= total)) {
                return;
            }
        }
    }

    private SpotifyPlaylistDTO mapToPlaylistDto(String accessToken,
                                                Map<String, Object> playlist,
                                                String lastPlayedAt,
                                                String currentUserId,
                                                boolean resolveLikedSummary) {
        Map<String, Object> externalUrls = asMap(playlist.get("external_urls"));
        Map<String, Object> owner = asMap(playlist.get("owner"));
        String playlistId = asString(playlist.get("id"));
        boolean hasLikedTracks = false;

        if (resolveLikedSummary && StringUtils.hasText(playlistId)) {
            try {
                hasLikedTracks = detectPlaylistHasLikedTracks(accessToken, playlistId);
            } catch (SpotifyApiException ex) {
                log.debug("No se pudo resolver favoritas para playlist {}: {}", playlistId, ex.getMessage());
            }
        }

        return SpotifyPlaylistDTO.builder()
            .id(playlistId)
            .name(asString(playlist.get("name")))
            .imageUrl(extractImageUrl(playlist))
            .tracksTotal(extractPlaylistTracksTotal(playlist))
            .publicPlaylist(asBoolean(playlist.get("public")))
            .ownerName(extractPlaylistOwnerName(playlist))
            .ownerId(asString(owner.get("id")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .lastPlayedAt(lastPlayedAt)
            .ownPlaylist(isOwnPlaylist(playlist, currentUserId))
            .collaborative(Boolean.TRUE.equals(asBoolean(playlist.get("collaborative"))))
            .hasLikedTracks(hasLikedTracks)
            .build();
    }

    private List<SpotifyPlaylistTrackDTO> getPlaylistTracksWithLikedStatus(String accessToken,
                                                                            String playlistId,
                                                                            int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_PLAYLIST_DETAIL_TRACKS));
        Map<String, Object> playlistTracks = getSpotifyResource(
            accessToken,
            "/playlists/" + playlistId + "/items?limit=" + safeLimit
        );

        List<Map<String, Object>> tracks = new ArrayList<>();
        LinkedHashSet<String> uniqueTrackIds = new LinkedHashSet<>();

        for (Map<String, Object> item : extractItems(playlistTracks)) {
            Map<String, Object> track = extractTrackFromPlaylistItem(item);
            if (track.isEmpty()) {
                continue;
            }

            tracks.add(track);
            String trackId = asString(track.get("id"));
            if (StringUtils.hasText(trackId)) {
                uniqueTrackIds.add(trackId);
            }
        }

        Map<String, Boolean> likedTracksById = getSavedTracksContainsLookup(accessToken, new ArrayList<>(uniqueTrackIds));

        List<SpotifyPlaylistTrackDTO> trackDtos = new ArrayList<>();
        for (Map<String, Object> track : tracks) {
            Map<String, Object> album = asMap(track.get("album"));
            Map<String, Object> externalUrls = asMap(track.get("external_urls"));
            String trackId = asString(track.get("id"));

            trackDtos.add(
                SpotifyPlaylistTrackDTO.builder()
                    .id(trackId)
                    .name(asString(track.get("name")))
                    .artists(extractNamedValues(track.get("artists"), "name"))
                    .albumName(asString(album.get("name")))
                    .durationMs(asNullableInteger(track.get("duration_ms")))
                    .externalUrl(asString(externalUrls.get("spotify")))
                    .liked(Boolean.TRUE.equals(likedTracksById.get(trackId)))
                    .build()
            );
        }

        return trackDtos;
    }

    private boolean detectPlaylistHasLikedTracks(String accessToken, String playlistId) {
        List<String> trackIds = extractPlaylistTrackIds(accessToken, playlistId, MAX_TRACKS_FOR_LIKED_PROBE);
        if (trackIds.isEmpty()) {
            return false;
        }

        Map<String, Boolean> likedTracksById = getSavedTracksContainsLookup(accessToken, trackIds);
        for (String trackId : trackIds) {
            if (Boolean.TRUE.equals(likedTracksById.get(trackId))) {
                return true;
            }
        }

        return false;
    }

    private List<String> extractPlaylistTrackIds(String accessToken, String playlistId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_PLAYLIST_DETAIL_TRACKS));
        Map<String, Object> playlistTracks = getSpotifyResource(
            accessToken,
            "/playlists/" + playlistId + "/items?limit=" + safeLimit
        );

        LinkedHashSet<String> trackIds = new LinkedHashSet<>();
        for (Map<String, Object> item : extractItems(playlistTracks)) {
            Map<String, Object> track = extractTrackFromPlaylistItem(item);
            String trackId = asString(track.get("id"));
            if (StringUtils.hasText(trackId)) {
                trackIds.add(trackId);
            }
        }

        return new ArrayList<>(trackIds);
    }

    private Map<String, Object> extractTrackFromPlaylistItem(Map<String, Object> playlistTrackItem) {
        Map<String, Object> track = asMap(playlistTrackItem.get("track"));
        if (track.isEmpty()) {
            track = asMap(playlistTrackItem.get("item"));
        }
        if (track.isEmpty()) {
            return Map.of();
        }

        if (Boolean.TRUE.equals(asBoolean(track.get("is_local")))) {
            return Map.of();
        }

        return track;
    }

    private Map<String, Boolean> getSavedTracksContainsLookup(String accessToken, List<String> trackIds) {
        Map<String, Boolean> likedTracksById = new HashMap<>();
        if (trackIds == null || trackIds.isEmpty()) {
            return likedTracksById;
        }

        LinkedHashSet<String> uniqueTrackIds = new LinkedHashSet<>();
        for (String trackId : trackIds) {
            if (StringUtils.hasText(trackId)) {
                uniqueTrackIds.add(trackId);
            }
        }

        try {
            return spotifyLibraryCompatibilityService.containsTracks(accessToken, new ArrayList<>(uniqueTrackIds));
        } catch (SpotifyApiException ex) {
            log.warn("No se pudo comprobar favoritos en library: {}", ex.getMessage());
            for (String trackId : uniqueTrackIds) {
                likedTracksById.put(trackId, false);
            }
            return likedTracksById;
        }
    }

    private String extractPlaylistOwnerName(Map<String, Object> playlist) {
        Map<String, Object> owner = asMap(playlist.get("owner"));
        String ownerDisplayName = asString(owner.get("display_name"));
        if (StringUtils.hasText(ownerDisplayName)) {
            return ownerDisplayName;
        }

        return asString(owner.get("id"));
    }

    private boolean isOwnPlaylist(Map<String, Object> playlist, String currentUserId) {
        if (!StringUtils.hasText(currentUserId)) {
            return false;
        }

        Map<String, Object> owner = asMap(playlist.get("owner"));
        String ownerId = asString(owner.get("id"));
        return StringUtils.hasText(ownerId) && currentUserId.equals(ownerId);
    }

    private SpotifyPlaylistDetailDTO buildUnavailablePlaylistDetail(String playlistId, String reason) {
        return SpotifyPlaylistDetailDTO.builder()
            .id(playlistId)
            .hasLikedTracks(false)
            .canLoadTracks(false)
            .unavailableReason(reason)
            .tracks(List.of())
            .build();
    }

    private String resolvePlaylistUnavailableReason(SpotifyApiException exception) {
        Integer statusCode = exception.getStatusCode();
        if (statusCode != null && statusCode == 403) {
            return "Spotify no permite leer los tracks de esta playlist con los permisos actuales.";
        }
        if (statusCode != null && statusCode == 404) {
            return "La playlist no esta disponible para este usuario o ya no existe.";
        }
        return "No se pudo cargar el detalle de la playlist.";
    }

    private boolean isAccessRestricted(SpotifyApiException exception) {
        Integer statusCode = exception.getStatusCode();
        return statusCode != null && (statusCode == 403 || statusCode == 404);
    }

    private SpotifyTrackDTO mapToTrackDto(Map<String, Object> track) {
        Map<String, Object> album = asMap(track.get("album"));
        Map<String, Object> externalUrls = asMap(track.get("external_urls"));

        return SpotifyTrackDTO.builder()
            .id(asString(track.get("id")))
            .name(asString(track.get("name")))
            .imageUrl(extractImageUrl(album))
            .artists(extractNamedValues(track.get("artists"), "name"))
            .albumName(asString(album.get("name")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .popularity(asInteger(track.get("popularity")))
            .build();
    }

    private SpotifyArtistDTO mapToArtistDto(Map<String, Object> artist) {
        Map<String, Object> followers = asMap(artist.get("followers"));
        Map<String, Object> externalUrls = asMap(artist.get("external_urls"));

        return SpotifyArtistDTO.builder()
            .id(asString(artist.get("id")))
            .name(asString(artist.get("name")))
            .imageUrl(extractImageUrl(artist))
            .followersTotal(asNullableInteger(followers.get("total")))
            .genres(asStringList(artist.get("genres")))
            .externalUrl(asString(externalUrls.get("spotify")))
            .popularity(asNullableInteger(artist.get("popularity")))
            .build();
    }

    private Map<String, Object> enrichArtistWithDetails(String accessToken, Map<String, Object> artist) {
        if (artistHasStats(artist)) {
            return artist;
        }

        String artistId = asString(artist.get("id"));
        if (!StringUtils.hasText(artistId)) {
            return artist;
        }

        try {
            Map<String, Object> detailedArtist = getSpotifyResource(accessToken, "/artists/" + artistId);
            if (detailedArtist.isEmpty()) {
                return artist;
            }

            Map<String, Object> merged = new HashMap<>(artist);
            merged.putAll(detailedArtist);
            return merged;
        } catch (SpotifyApiException ex) {
            log.warn("No se pudo enriquecer el artista {} con detalle: {}", artistId, ex.getMessage());
            return artist;
        }
    }

    private boolean artistHasStats(Map<String, Object> artist) {
        Map<String, Object> followers = asMap(artist.get("followers"));
        boolean hasFollowers = asNullableInteger(followers.get("total")) != null;
        boolean hasGenres = !asStringList(artist.get("genres")).isEmpty();
        boolean hasPopularity = asNullableInteger(artist.get("popularity")) != null;
        return hasFollowers || hasGenres || hasPopularity;
    }

    private Integer extractPlaylistTracksTotal(Map<String, Object> playlist) {
        Map<String, Object> tracks = asMap(playlist.get("tracks"));
        Integer tracksTotal = asInteger(tracks.get("total"));
        if (tracksTotal > 0) {
            return tracksTotal;
        }

        Map<String, Object> itemsSummary = asMap(playlist.get("items"));
        Integer itemsTotal = asInteger(itemsSummary.get("total"));
        if (itemsTotal > 0) {
            return itemsTotal;
        }

        Object itemsObj = itemsSummary.get("items");
        if (itemsObj instanceof List<?> listItems) {
            return listItems.size();
        }

        return 0;
    }

    private String extractPlaylistId(Map<String, Object> context) {
        if (!PLAYLIST_CONTEXT_TYPE.equals(asString(context.get("type")))) {
            return null;
        }

        String uri = asString(context.get("uri"));
        if (StringUtils.hasText(uri) && uri.startsWith("spotify:playlist:")) {
            return uri.substring("spotify:playlist:".length());
        }

        String href = asString(context.get("href"));
        if (StringUtils.hasText(href) && href.contains("/playlists/")) {
            int slashIndex = href.lastIndexOf('/');
            if (slashIndex >= 0 && slashIndex < href.length() - 1) {
                return href.substring(slashIndex + 1);
            }
        }

        return null;
    }

    private String extractImageUrl(Map<String, Object> playlist) {
        Object imagesObj = playlist.get("images");
        if (!(imagesObj instanceof List<?> images) || images.isEmpty()) {
            return null;
        }

        Map<String, Object> firstImage = asMap(images.get(0));
        return asString(firstImage.get("url"));
    }

    private List<String> extractNamedValues(Object value, String fieldName) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (Object entry : list) {
            Map<String, Object> map = asMap(entry);
            String fieldValue = asString(map.get(fieldName));
            if (StringUtils.hasText(fieldValue)) {
                values.add(fieldValue);
            }
        }
        return values;
    }

    private List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (Object entry : list) {
            String textValue = asString(entry);
            if (StringUtils.hasText(textValue)) {
                values.add(textValue);
            }
        }
        return values;
    }

    private List<Map<String, Object>> extractItems(Map<String, Object> response) {
        Object itemsObj = response.get("items");
        if (!(itemsObj instanceof List<?> items)) {
            return List.of();
        }

        List<Map<String, Object>> parsedItems = new ArrayList<>();
        for (Object item : items) {
            parsedItems.add(asMap(item));
        }
        return parsedItems;
    }

    private Map<String, Object> getSpotifyResource(String accessToken, String uri) {
        String safeAccessToken = Objects.requireNonNull(accessToken, "Spotify access token es obligatorio");
        String safeUri = Objects.requireNonNull(uri, "La URI de Spotify es obligatoria");

        try {
            Map<?, ?> response = spotifyWebClient.get()
                .uri(safeUri)
                .headers(headers -> headers.setBearerAuth(safeAccessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return response != null ? asMap(response) : Collections.emptyMap();
        } catch (WebClientResponseException ex) {
            String message = String.format(
                "Error al consultar Spotify (%s): %s",
                ex.getStatusCode().value(),
                ex.getResponseBodyAsString()
            );
            throw new SpotifyApiException(message, ex.getStatusCode().value(), ex);
        } catch (Exception ex) {
            throw new SpotifyApiException("Error inesperado al consultar Spotify", ex);
        }
    }

    @SuppressWarnings("unused")
    private List<Object> getSpotifyResourceList(String accessToken, String uri) {
        String safeAccessToken = Objects.requireNonNull(accessToken, "Spotify access token es obligatorio");
        String safeUri = Objects.requireNonNull(uri, "La URI de Spotify es obligatoria");

        try {
            List<?> response = spotifyWebClient.get()
                .uri(safeUri)
                .headers(headers -> headers.setBearerAuth(safeAccessToken))
                .retrieve()
                .bodyToMono(List.class)
                .block();

            if (response == null) {
                return List.of();
            }

            return new ArrayList<>(response);
        } catch (WebClientResponseException ex) {
            String message = String.format(
                "Error al consultar Spotify (%s): %s",
                ex.getStatusCode().value(),
                ex.getResponseBodyAsString()
            );
            throw new SpotifyApiException(message, ex.getStatusCode().value(), ex);
        } catch (Exception ex) {
            throw new SpotifyApiException("Error inesperado al consultar Spotify", ex);
        }
    }

    private Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }

        Map<String, Object> map = new HashMap<>();
        rawMap.forEach((key, mapValue) -> {
            if (key != null) {
                map.put(String.valueOf(key), mapValue);
            }
        });
        return map;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Integer asInteger(Object value) {
        Integer parsed = asNullableInteger(value);
        return parsed != null ? parsed : 0;
    }

    private Integer asNullableInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String text && text.matches("\\d+")) {
            return Integer.parseInt(text);
        }

        return null;
    }

    private Boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }

        if (value instanceof String text && StringUtils.hasText(text)) {
            return Boolean.parseBoolean(text);
        }

        return null;
    }
}

