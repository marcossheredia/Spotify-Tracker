package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyPagedResponseDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.request.SpotifyPlaylistAutomationRequestDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistAutomationResponseDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.exception.ResourceNotFoundException;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpotifyPlaylistManagementService {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;
    private final SpotifyService spotifyService;

    public SpotifyPagedResponseDTO<SpotifyPlaylistDTO> getAllPlaylists(String accessToken, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);

        Map<String, Object> response = spotifyApiClient.getMap(
            accessToken,
            "/me/playlists?limit=" + safeLimit + "&offset=" + safeOffset
        );

        String meId = mapper.asString(spotifyApiClient.getMap(accessToken, "/me").get("id"));
        List<SpotifyPlaylistDTO> items = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(response)) {
            SpotifyPlaylistDTO base = mapper.toPlaylist(item);
            String ownerId = mapper.asString(mapper.asMap(item.get("owner")).get("id"));
            items.add(SpotifyPlaylistDTO.builder()
                .id(base.getId())
                .name(base.getName())
                .imageUrl(base.getImageUrl())
                .tracksTotal(base.getTracksTotal())
                .ownerName(base.getOwnerName())
                .externalUrl(base.getExternalUrl())
                .lastPlayedAt(null)
                .ownPlaylist(meId != null && meId.equals(ownerId))
                .collaborative(base.getCollaborative())
                .hasLikedTracks(false)
                .build());
        }

        return SpotifyPagedResponseDTO.<SpotifyPlaylistDTO>builder()
            .items(items)
            .limit(safeLimit)
            .offset(safeOffset)
            .total(mapper.asNullableInteger(response.get("total")))
            .hasNext(response.get("next") != null)
            .build();
    }

    public SpotifyPlaylistAutomationResponseDTO createTopTracksPlaylist(String accessToken,
                                                                        SpotifyPlaylistAutomationRequestDTO request) {
        if (request == null) {
            request = new SpotifyPlaylistAutomationRequestDTO();
        }
        int safeLimit = normalizeLimit(request.getLimit());
        String safeTimeRange = normalizeTimeRange(request.getTimeRange());
        String playlistName = resolvePlaylistName(request.getName(), safeTimeRange);

        List<String> topTrackUris = new ArrayList<>();
        spotifyService.getTopTracks(accessToken, safeLimit, safeTimeRange).forEach(track -> {
            if (track.getId() != null && !track.getId().isBlank()) {
                topTrackUris.add("spotify:track:" + track.getId());
            }
        });
        List<String> uniqueTopTrackUris = new ArrayList<>(new LinkedHashSet<>(topTrackUris));

        if (uniqueTopTrackUris.isEmpty()) {
            throw new ResourceNotFoundException("No hay suficientes datos de escucha para este periodo.");
        }

        Map<String, Object> playlistResponse;
        try {
            playlistResponse = spotifyApiClient.postMap(
                accessToken,
                "/me/playlists",
                Map.of(
                    "name", playlistName,
                    "description", "Playlist generada automaticamente con tus canciones mas escuchadas en Spotify Tracker.",
                    "public", Boolean.TRUE.equals(request.getPublicPlaylist())
                )
            );
        } catch (SpotifyApiException ex) {
            throw mapPlaylistCreationError(ex);
        }

        String playlistId = mapper.asString(playlistResponse.get("id"));
        if (playlistId != null && !playlistId.isBlank() && !uniqueTopTrackUris.isEmpty()) {
            try {
                for (int i = 0; i < uniqueTopTrackUris.size(); i += 100) {
                    int end = Math.min(i + 100, uniqueTopTrackUris.size());
                    spotifyApiClient.postNoContent(
                        accessToken,
                        "/playlists/" + playlistId + "/items",
                        Map.of("uris", uniqueTopTrackUris.subList(i, end))
                    );
                }
            } catch (SpotifyApiException ex) {
                throw mapPlaylistAddTracksError(ex);
            }
        }

        return SpotifyPlaylistAutomationResponseDTO.builder()
            .playlistId(playlistId)
            .playlistName(mapper.asString(playlistResponse.get("name")))
            .externalUrl(mapper.asString(mapper.asMap(playlistResponse.get("external_urls")).get("spotify")))
            .tracksAdded(uniqueTopTrackUris.size())
            .timeRange(safeTimeRange)
            .build();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 25;
        }
        int value = limit;
        if (value == 10 || value == 25 || value == 50) {
            return value;
        }
        return 25;
    }

    private String normalizeTimeRange(String raw) {
        String value = SpotifyTimeRange.fromQuery(raw).getApiValue();
        if ("short_term".equals(value) || "medium_term".equals(value) || "long_term".equals(value)) {
            return value;
        }
        return "medium_term";
    }

    private String resolvePlaylistName(String rawName, String timeRange) {
        if (StringUtils.hasText(rawName)) {
            return rawName.trim();
        }
        return switch (timeRange) {
            case "short_term" -> "Mis top canciones - Últimas 4 semanas";
            case "long_term" -> "Mis top canciones - Último año";
            default -> "Mis top canciones - Últimos 6 meses";
        };
    }

    private SpotifyApiException mapPlaylistCreationError(SpotifyApiException ex) {
        Integer status = ex.getStatusCode();
        if (status != null) {
            if (status == 401) {
                return new SpotifyApiException("Tu sesion con Spotify ha caducado. Vuelve a iniciar sesion.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 403) {
                return new SpotifyApiException("Spotify no ha permitido crear la playlist. Revisa permisos.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 429) {
                return new SpotifyApiException("Spotify ha limitado temporalmente las peticiones. Intentalo mas tarde.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
        }
        return ex;
    }

    private SpotifyApiException mapPlaylistAddTracksError(SpotifyApiException ex) {
        Integer status = ex.getStatusCode();
        if (status != null) {
            if (status == 401) {
                return new SpotifyApiException("Tu sesion con Spotify ha caducado. Vuelve a iniciar sesion.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 403) {
                return new SpotifyApiException("Spotify no ha permitido anadir canciones a la playlist. Revisa permisos.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 429) {
                return new SpotifyApiException("Spotify ha limitado temporalmente las peticiones. Intentalo mas tarde.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
        }
        return ex;
    }
}
