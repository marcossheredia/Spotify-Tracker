package com.tfg.spotifytracker.dto.playback.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PlaytimeHistoryResponseDTO {
    private Instant from;
    private Instant to;
    private String granularity;
    private Long totalPlaytimeMs;
    private Long totalReproducciones;
    private List<PlaytimeHistoryPointDTO> points;
}
