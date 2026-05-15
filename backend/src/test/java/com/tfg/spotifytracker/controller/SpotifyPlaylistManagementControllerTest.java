package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.spotify.playlist.request.SpotifyPlaylistAutomationRequestDTO;
import com.tfg.spotifytracker.dto.spotify.playlist.response.SpotifyPlaylistAutomationResponseDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifyPlaylistManagementService;
import com.tfg.spotifytracker.service.SpotifyTokenService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpotifyPlaylistManagementControllerTest {

    private final SpotifyPlaylistManagementService spotifyPlaylistManagementService = mock(SpotifyPlaylistManagementService.class);
    private final SpotifyTokenService spotifyTokenService = mock(SpotifyTokenService.class);
    private final SpotifyPlaylistManagementController controller = new SpotifyPlaylistManagementController(
        spotifyPlaylistManagementService,
        spotifyTokenService
    );

    @Test
    void shouldCreateTopTracksPlaylist() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).spotifyId("u1").accessToken("token").build();
        SpotifyPlaylistAutomationRequestDTO request = new SpotifyPlaylistAutomationRequestDTO();
        request.setLimit(25);
        request.setTimeRange("medium_term");

        when(spotifyTokenService.getValidAccessToken(usuario)).thenReturn("valid-token");
        when(spotifyPlaylistManagementService.createTopTracksPlaylist(eq("valid-token"), any()))
            .thenReturn(SpotifyPlaylistAutomationResponseDTO.builder()
                .playlistId("playlist-123")
                .playlistName("Auto playlist")
                .tracksAdded(25)
                .timeRange("medium_term")
                .build());

        var response = controller.createTopTracksPlaylist(usuario, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlaylistId()).isEqualTo("playlist-123");
    }

    @Test
    void shouldThrowUnauthorizedWhenMissingPrincipal() {
        assertThrows(UnauthorizedException.class,
            () -> controller.createTopTracksPlaylist(null, new SpotifyPlaylistAutomationRequestDTO()));
    }
}
