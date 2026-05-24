package com.tfg.spotifytracker.dto.playback.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
/**
 * Clase funcional: RecentPlaybackSyncResponseDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class RecentPlaybackSyncResponseDTO {
    private Long addedTracks;
    private Long addedDurationMs;
    private Long totalPlaytimeMs;
    private Long totalReproducciones;
    private Instant lastRecentlyPlayedAt;
    private Instant lastSyncAt;
}
