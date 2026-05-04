package com.tfg.spotifytracker.dto.playback.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RecentPlaybackSyncResponseDTO {
    private Long addedTracks;
    private Long addedDurationMs;
    private Long totalPlaytimeMs;
    private Long totalReproducciones;
    private Instant lastRecentlyPlayedAt;
    private Instant lastSyncAt;
}
