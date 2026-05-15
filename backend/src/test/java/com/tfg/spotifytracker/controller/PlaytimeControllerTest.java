package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.playback.response.PlaytimeHistoryResponseDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.RecentPlaybackSyncService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaytimeControllerTest {

    private final RecentPlaybackSyncService recentPlaybackSyncService = mock(RecentPlaybackSyncService.class);
    private final PlaytimeController controller = new PlaytimeController(recentPlaybackSyncService);

    @Test
    void shouldReturnPlaytimeHistory() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).accessToken("token").build();
        when(recentPlaybackSyncService.getPlaytimeHistoryByDate(any(), any(), any(), eq("day")))
            .thenReturn(PlaytimeHistoryResponseDTO.builder()
                .from(Instant.parse("2026-01-01T00:00:00Z"))
                .to(Instant.parse("2026-01-05T00:00:00Z"))
                .granularity("day")
                .totalPlaytimeMs(12_000L)
                .totalReproducciones(6L)
                .points(List.of())
                .build());

        var response = controller.getHistory(usuario, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-05"), "day");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalPlaytimeMs()).isEqualTo(12_000L);
    }

    @Test
    void shouldThrowUnauthorizedWhenNoAccessToken() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).accessToken(null).build();
        assertThrows(UnauthorizedException.class,
            () -> controller.getHistory(usuario, null, null, "day"));
    }
}
