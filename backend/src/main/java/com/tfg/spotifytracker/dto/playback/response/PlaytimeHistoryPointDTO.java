package com.tfg.spotifytracker.dto.playback.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PlaytimeHistoryPointDTO {
    private Instant periodStart;
    private Long totalPlaytimeMs;
    private Long totalReproducciones;
}
