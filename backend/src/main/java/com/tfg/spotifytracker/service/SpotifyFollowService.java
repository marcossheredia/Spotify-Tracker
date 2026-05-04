package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyActionResultDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyArtistDTO;
import com.tfg.spotifytracker.dto.spotify.response.SpotifyPagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpotifyFollowService {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;
    private final SpotifyLibraryCompatibilityService spotifyLibraryCompatibilityService;

    public SpotifyPagedResponseDTO<SpotifyArtistDTO> getFollowedArtists(String accessToken, int limit, String after) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String afterQuery = (after != null && !after.isBlank()) ? "&after=" + after.trim() : "";

        Map<String, Object> response = spotifyApiClient.getMap(
            accessToken,
            "/me/following?type=artist&limit=" + safeLimit + afterQuery
        );

        Map<String, Object> artistsPage = mapper.asMap(response.get("artists"));
        List<SpotifyArtistDTO> items = new ArrayList<>();
        for (Map<String, Object> item : mapper.extractItems(artistsPage)) {
            items.add(mapper.toArtist(item));
        }

        Map<String, Object> cursors = mapper.asMap(artistsPage.get("cursors"));
        String nextCursor = mapper.asString(cursors.get("after"));

        return SpotifyPagedResponseDTO.<SpotifyArtistDTO>builder()
            .items(items)
            .limit(safeLimit)
            .offset(0)
            .total(mapper.asNullableInteger(artistsPage.get("total")))
            .nextCursor(nextCursor)
            .hasNext(nextCursor != null)
            .build();
    }

    public SpotifyActionResultDTO followArtist(String accessToken, String artistId) {
        spotifyLibraryCompatibilityService.followArtist(accessToken, artistId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("follow-artist")
            .message("Artista seguido correctamente")
            .build();
    }

    public SpotifyActionResultDTO unfollowArtist(String accessToken, String artistId) {
        spotifyLibraryCompatibilityService.unfollowArtist(accessToken, artistId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("unfollow-artist")
            .message("Ya no sigues a este artista")
            .build();
    }

    public SpotifyActionResultDTO followPlaylist(String accessToken, String playlistId) {
        spotifyLibraryCompatibilityService.followPlaylist(accessToken, playlistId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("follow-playlist")
            .message("Playlist seguida correctamente")
            .build();
    }

    public SpotifyActionResultDTO unfollowPlaylist(String accessToken, String playlistId) {
        spotifyLibraryCompatibilityService.unfollowPlaylist(accessToken, playlistId);
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action("unfollow-playlist")
            .message("Has dejado de seguir la playlist")
            .build();
    }
}
