package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.playback.response.PlaytimeHistoryResponseDTO;
import com.tfg.spotifytracker.dto.playback.response.RecentPlaybackSyncResponseDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.entity.UsuarioEstadisticas;
import com.tfg.spotifytracker.repository.ReproduccionRecienteRepository;
import com.tfg.spotifytracker.repository.UsuarioEstadisticasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RecentPlaybackSyncServiceTest {

    @Mock
    private UsuarioEstadisticasRepository usuarioEstadisticasRepository;
    @Mock
    private ReproduccionRecienteRepository reproduccionRecienteRepository;
    @Mock
    private SpotifyService spotifyService;
    @Mock
    private SpotifyTokenService spotifyTokenService;

    @InjectMocks
    private RecentPlaybackSyncService service;

    private Usuario usuario;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        usuario = Usuario.builder().id(userId).accessToken("token").build();
    }

    @Test
    void shouldSaveNewRecentPlaybacksAndUpdateStats() {
        UsuarioEstadisticas stats = UsuarioEstadisticas.builder()
            .usuarioId(userId)
            .totalPlaytimeMs(1_000L)
            .totalReproducciones(2L)
            .build();

        when(usuarioEstadisticasRepository.findById(userId)).thenReturn(Optional.of(stats));
        when(spotifyTokenService.getValidAccessToken(usuario)).thenReturn("valid-token");
        when(spotifyService.getRecentlyPlayed(eq("valid-token"), eq((Long) null), eq((Long) null), anyInt()))
            .thenReturn(pageWithItems(List.of(
                item("track-1", "2026-01-01T10:00:00Z", 120_000),
                item("track-2", "2026-01-01T10:05:00Z", 180_000)
            )));
        when(reproduccionRecienteRepository.existsByUsuarioIdAndSpotifyTrackIdAndPlayedAtMs(any(), any(), anyLong()))
            .thenReturn(false);
        when(usuarioEstadisticasRepository.save(any(UsuarioEstadisticas.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RecentPlaybackSyncResponseDTO response = service.syncRecentPlaytime(usuario);

        assertThat(response.getAddedTracks()).isEqualTo(2L);
        assertThat(response.getAddedDurationMs()).isEqualTo(300_000L);
        assertThat(response.getTotalPlaytimeMs()).isEqualTo(301_000L);
        assertThat(response.getTotalReproducciones()).isEqualTo(4L);
        verify(reproduccionRecienteRepository, times(2)).save(any());
        verify(usuarioEstadisticasRepository).save(any(UsuarioEstadisticas.class));
    }

    @Test
    void shouldNotDuplicateExistingPlaybacks() {
        long baselineMs = Instant.parse("2026-01-01T00:00:00Z").toEpochMilli();
        UsuarioEstadisticas stats = UsuarioEstadisticas.builder()
            .usuarioId(userId)
            .totalPlaytimeMs(10_000L)
            .totalReproducciones(5L)
            .lastRecentlyPlayedMs(baselineMs)
            .build();

        when(usuarioEstadisticasRepository.findById(userId)).thenReturn(Optional.of(stats));
        when(spotifyTokenService.getValidAccessToken(usuario)).thenReturn("valid-token");
        when(spotifyService.getRecentlyPlayed(eq("valid-token"), eq(baselineMs), anyInt()))
            .thenReturn(pageWithItems(List.of(item("track-1", "2026-01-01T01:00:00Z", 200_000))));
        when(reproduccionRecienteRepository.existsByUsuarioIdAndSpotifyTrackIdAndPlayedAtMs(any(), any(), anyLong()))
            .thenReturn(true);
        when(usuarioEstadisticasRepository.save(any(UsuarioEstadisticas.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RecentPlaybackSyncResponseDTO response = service.syncRecentPlaytime(usuario);

        assertThat(response.getAddedTracks()).isZero();
        assertThat(response.getAddedDurationMs()).isZero();
        assertThat(response.getTotalPlaytimeMs()).isEqualTo(10_000L);
        assertThat(response.getTotalReproducciones()).isEqualTo(5L);
        verify(reproduccionRecienteRepository, never()).save(any());
    }

    @Test
    void shouldHandleEmptySpotifyRecentListWithoutChangingStats() {
        UsuarioEstadisticas stats = UsuarioEstadisticas.builder()
            .usuarioId(userId)
            .totalPlaytimeMs(5_000L)
            .totalReproducciones(3L)
            .build();

        when(usuarioEstadisticasRepository.findById(userId)).thenReturn(Optional.of(stats));
        when(spotifyTokenService.getValidAccessToken(usuario)).thenReturn("valid-token");
        when(spotifyService.getRecentlyPlayed(eq("valid-token"), eq((Long) null), eq((Long) null), anyInt()))
            .thenReturn(Map.of("items", List.of()));
        when(usuarioEstadisticasRepository.save(any(UsuarioEstadisticas.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RecentPlaybackSyncResponseDTO response = service.syncRecentPlaytime(usuario);

        assertThat(response.getAddedTracks()).isZero();
        assertThat(response.getTotalPlaytimeMs()).isEqualTo(5_000L);
        assertThat(response.getTotalReproducciones()).isEqualTo(3L);
    }

    @Test
    void shouldIgnoreInvalidOrNullTrackItemsSafely() {
        UsuarioEstadisticas stats = UsuarioEstadisticas.builder()
            .usuarioId(userId)
            .totalPlaytimeMs(0L)
            .totalReproducciones(0L)
            .build();

        when(usuarioEstadisticasRepository.findById(userId)).thenReturn(Optional.of(stats));
        when(spotifyTokenService.getValidAccessToken(usuario)).thenReturn("valid-token");
        when(spotifyService.getRecentlyPlayed(eq("valid-token"), eq((Long) null), eq((Long) null), anyInt()))
            .thenReturn(pageWithItems(List.of(
                Map.of("track", Map.of("id", ""), "played_at", "2026-01-01T10:00:00Z"),
                Map.of("track", Map.of("id", "track-1", "duration_ms", 120_000), "played_at", "bad-date"),
                item("track-2", "2026-01-01T10:10:00Z", 90_000)
            )));
        when(reproduccionRecienteRepository.existsByUsuarioIdAndSpotifyTrackIdAndPlayedAtMs(any(), any(), anyLong()))
            .thenReturn(false);
        when(usuarioEstadisticasRepository.save(any(UsuarioEstadisticas.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RecentPlaybackSyncResponseDTO response = service.syncRecentPlaytime(usuario);

        assertThat(response.getAddedTracks()).isEqualTo(1L);
        assertThat(response.getAddedDurationMs()).isEqualTo(90_000L);
    }

    @Test
    void shouldReturnZeroStatsWhenUserHasNoStatsYet() {
        ArgumentCaptor<UsuarioEstadisticas> captor = ArgumentCaptor.forClass(UsuarioEstadisticas.class);

        when(usuarioEstadisticasRepository.findById(userId)).thenReturn(Optional.empty());
        when(usuarioEstadisticasRepository.save(any(UsuarioEstadisticas.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RecentPlaybackSyncResponseDTO response = service.getPlaytimeStats(usuario);

        assertThat(response.getTotalPlaytimeMs()).isZero();
        assertThat(response.getTotalReproducciones()).isZero();
        verify(usuarioEstadisticasRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(userId);
    }

    @Test
    void shouldGroupPlaytimeHistoryByDay() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-03T23:59:59Z");

        when(reproduccionRecienteRepository.findPlaytimeHistory(eq(userId), eq(from), eq(to), eq("day")))
            .thenReturn(List.of(
                new Object[]{Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")), 1000L, 2L},
                new Object[]{Timestamp.from(Instant.parse("2026-01-03T00:00:00Z")), 2000L, 1L}
            ));

        PlaytimeHistoryResponseDTO response = service.getPlaytimeHistory(usuario, from, to, "day");

        assertThat(response.getTotalPlaytimeMs()).isEqualTo(3000L);
        assertThat(response.getTotalReproducciones()).isEqualTo(3L);
        assertThat(response.getPoints()).hasSize(3);
        assertThat(response.getPoints().get(0).getPeriodStart()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(response.getPoints().get(1).getTotalPlaytimeMs()).isZero();
        assertThat(response.getPoints().get(2).getPeriodStart()).isEqualTo(Instant.parse("2026-01-03T00:00:00Z"));
    }

    @Test
    void shouldGroupPlaytimeHistoryByMonthAndRespectDateRange() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-03-31T23:59:59Z");

        when(reproduccionRecienteRepository.findPlaytimeHistory(eq(userId), eq(from), eq(to), eq("month")))
            .thenReturn(List.of(
                new Object[]{Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")), 3000L, 3L},
                new Object[]{Timestamp.from(Instant.parse("2026-03-01T00:00:00Z")), 2000L, 2L}
            ));

        PlaytimeHistoryResponseDTO response = service.getPlaytimeHistory(usuario, from, to, "month");

        assertThat(response.getPoints()).hasSize(3);
        assertThat(response.getPoints().get(0).getTotalPlaytimeMs()).isEqualTo(3000L);
        assertThat(response.getPoints().get(1).getPeriodStart()).isEqualTo(Instant.parse("2026-02-01T00:00:00Z"));
        assertThat(response.getPoints().get(2).getTotalPlaytimeMs()).isEqualTo(2000L);
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoDataExists() {
        Instant to = Instant.parse("2026-01-31T23:59:59Z");

        when(reproduccionRecienteRepository.findFirstPlayedAt(userId)).thenReturn(null);

        PlaytimeHistoryResponseDTO response = service.getPlaytimeHistory(usuario, null, to, "day");

        assertThat(response.getPoints()).isEmpty();
        assertThat(response.getTotalPlaytimeMs()).isZero();
        assertThat(response.getTotalReproducciones()).isZero();
    }

    @Test
    void shouldRejectInvalidGranularity() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getPlaytimeHistory(usuario, Instant.now(), Instant.now(), "year"));
    }

    private Map<String, Object> pageWithItems(List<Map<String, Object>> items) {
        return Map.of("items", items, "cursors", Map.of());
    }

    private Map<String, Object> item(String trackId, String playedAt, int durationMs) {
        return Map.of(
            "track", Map.of(
                "id", trackId,
                "name", "Track " + trackId,
                "duration_ms", durationMs,
                "artists", List.of(Map.of("name", "Artist")),
                "album", Map.of("name", "Album", "images", List.of(Map.of("url", "http://img"))),
                "external_urls", Map.of("spotify", "http://spotify/track/" + trackId)
            ),
            "played_at", playedAt,
            "context", Map.of("type", "playlist", "uri", "spotify:playlist:1")
        );
    }
}
