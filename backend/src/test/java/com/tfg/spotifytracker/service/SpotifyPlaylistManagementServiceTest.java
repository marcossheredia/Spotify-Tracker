package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.request.SpotifyPlaylistAutomationRequestDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistAutomationResponseDTO;
import com.tfg.spotifytracker.exception.ResourceNotFoundException;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistManagementServiceTest {

    @Mock
    private SpotifyApiClient spotifyApiClient;
    @Mock
    private SpotifyDtoMapper mapper;
    @Mock
    private SpotifyService spotifyService;

    @InjectMocks
    private SpotifyPlaylistManagementService service;

    @Test
    void shouldCreateAutomaticPlaylistFromTopTracks() {
        SpotifyPlaylistAutomationRequestDTO request = new SpotifyPlaylistAutomationRequestDTO();
        request.setName("Mi playlist");
        request.setTimeRange("short_term");
        request.setLimit(10);
        request.setPublicPlaylist(true);

        when(spotifyService.getTopTracks("token", 10, "short_term")).thenReturn(List.of(
            SpotifyTrackDTO.builder().id("t1").build(),
            SpotifyTrackDTO.builder().id("t2").build()
        ));
        when(spotifyApiClient.postMap(eq("token"), eq("/me/playlists"), anyMap())).thenReturn(Map.of(
            "id", "playlist-1",
            "name", "Mi playlist",
            "external_urls", Map.of("spotify", "https://spotify/playlist-1")
        ));
        when(mapper.asString("playlist-1")).thenReturn("playlist-1");
        when(mapper.asString("Mi playlist")).thenReturn("Mi playlist");
        when(mapper.asMap(Map.of("spotify", "https://spotify/playlist-1"))).thenReturn(Map.of("spotify", "https://spotify/playlist-1"));
        when(mapper.asString("https://spotify/playlist-1")).thenReturn("https://spotify/playlist-1");

        SpotifyPlaylistAutomationResponseDTO response = service.createTopTracksPlaylist("token", request);

        assertThat(response.getPlaylistId()).isEqualTo("playlist-1");
        assertThat(response.getTracksAdded()).isEqualTo(2);
        assertThat(response.getTimeRange()).isEqualTo("short_term");
        verify(spotifyApiClient).postNoContent(eq("token"), contains("/playlists/playlist-1/items"), anyMap());
    }

    @Test
    void shouldNotCreatePlaylistWhenNoTopTracksFound() {
        SpotifyPlaylistAutomationRequestDTO request = new SpotifyPlaylistAutomationRequestDTO();
        request.setTimeRange("medium_term");
        request.setLimit(25);

        when(spotifyService.getTopTracks("token", 25, "medium_term")).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.createTopTracksPlaylist("token", request));
        verify(spotifyApiClient, never()).postMap(eq("token"), eq("/me/playlists"), anyMap());
    }

    @Test
    void shouldUseSafeDefaultsForInvalidRangeAndLimit() {
        SpotifyPlaylistAutomationRequestDTO request = new SpotifyPlaylistAutomationRequestDTO();
        request.setTimeRange("bad_term");
        request.setLimit(99);

        when(spotifyService.getTopTracks("token", 25, "short_term")).thenReturn(List.of(
            SpotifyTrackDTO.builder().id("t1").build()
        ));
        when(spotifyApiClient.postMap(eq("token"), eq("/me/playlists"), anyMap())).thenReturn(Map.of(
            "id", "playlist-2",
            "name", "Auto",
            "external_urls", Map.of("spotify", "https://spotify/playlist-2")
        ));
        when(mapper.asString("playlist-2")).thenReturn("playlist-2");
        when(mapper.asString("Auto")).thenReturn("Auto");
        when(mapper.asMap(Map.of("spotify", "https://spotify/playlist-2"))).thenReturn(Map.of("spotify", "https://spotify/playlist-2"));
        when(mapper.asString("https://spotify/playlist-2")).thenReturn("https://spotify/playlist-2");

        SpotifyPlaylistAutomationResponseDTO response = service.createTopTracksPlaylist("token", request);

        assertThat(response.getTimeRange()).isEqualTo("short_term");
        verify(spotifyService).getTopTracks("token", 25, "short_term");
    }

    @Test
    void shouldPropagateSpotifyErrorWhenCreatePlaylistFails() {
        SpotifyPlaylistAutomationRequestDTO request = new SpotifyPlaylistAutomationRequestDTO();
        request.setLimit(10);
        request.setTimeRange("short_term");

        when(spotifyService.getTopTracks("token", 10, "short_term")).thenReturn(List.of(
            SpotifyTrackDTO.builder().id("t1").build()
        ));
        when(spotifyApiClient.postMap(eq("token"), eq("/me/playlists"), anyMap()))
            .thenThrow(new SpotifyApiException("spotify failed", 401));

        SpotifyApiException ex = assertThrows(SpotifyApiException.class,
            () -> service.createTopTracksPlaylist("token", request));

        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    void shouldPropagateSpotifyErrorWhenAddingTracksFails() {
        SpotifyPlaylistAutomationRequestDTO request = new SpotifyPlaylistAutomationRequestDTO();
        request.setLimit(10);
        request.setTimeRange("long_term");

        when(spotifyService.getTopTracks("token", 10, "long_term")).thenReturn(List.of(
            SpotifyTrackDTO.builder().id("t1").build()
        ));
        when(spotifyApiClient.postMap(eq("token"), eq("/me/playlists"), anyMap())).thenReturn(Map.of(
            "id", "playlist-3",
            "name", "Auto",
            "external_urls", Map.of("spotify", "https://spotify/playlist-3")
        ));
        when(mapper.asString("playlist-3")).thenReturn("playlist-3");
        doThrow(new SpotifyApiException("rate limited", 429))
            .when(spotifyApiClient)
            .postNoContent(eq("token"), contains("/playlists/playlist-3/items"), anyMap());

        SpotifyApiException ex = assertThrows(SpotifyApiException.class,
            () -> service.createTopTracksPlaylist("token", request));

        assertThat(ex.getStatusCode()).isEqualTo(429);
    }
}
