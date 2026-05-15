package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyPagedResponseDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.request.SpotifyPlaylistAutomationRequestDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistAutomationResponseDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistDTO;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
        int safeLimit = Math.max(1, Math.min(request.getLimit() == null ? 20 : request.getLimit(), 50));
        String safeTimeRange = SpotifyTimeRange.fromQuery(request.getTimeRange()).getApiValue();

        List<String> topTrackUris = new ArrayList<>();
        spotifyService.getTopTracks(accessToken, safeLimit, safeTimeRange).forEach(track -> {
            if (track.getId() != null && !track.getId().isBlank()) {
                topTrackUris.add("spotify:track:" + track.getId());
            }
        });

        String userId = mapper.asString(spotifyApiClient.getMap(accessToken, "/me").get("id"));
        if (!StringUtils.hasText(userId)) {
            throw new SpotifyApiException("No se pudo identificar el usuario de Spotify");
        }

        Map<String, Object> playlistResponse = spotifyApiClient.postMap(
            accessToken,
            "/users/" + userId + "/playlists",
            Map.of(
                "name", request.getName() == null || request.getName().isBlank()
                    ? "Top del mes - Spotify Tracker"
                    : request.getName(),
                "description", request.getDescription() == null || request.getDescription().isBlank()
                    ? "Playlist autogenerada por Spotify Tracker"
                    : request.getDescription(),
                "public", Boolean.TRUE.equals(request.getPublicPlaylist())
            )
        );

        String playlistId = mapper.asString(playlistResponse.get("id"));
        if (playlistId != null && !playlistId.isBlank() && !topTrackUris.isEmpty()) {
            spotifyApiClient.postNoContent(
                accessToken,
                "/playlists/" + playlistId + "/items",
                Map.of("uris", topTrackUris)
            );
        }

        return SpotifyPlaylistAutomationResponseDTO.builder()
            .playlistId(playlistId)
            .playlistName(mapper.asString(playlistResponse.get("name")))
            .externalUrl(mapper.asString(mapper.asMap(playlistResponse.get("external_urls")).get("spotify")))
            .tracksAdded(topTrackUris.size())
            .build();
    }
}
