package com.tfg.spotifytracker.dto.playback.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
/**
 * Clase funcional: PlaytimeHistoryPointDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class PlaytimeHistoryPointDTO {
    private Instant periodStart;
    private Long totalPlaytimeMs;
    private Long totalReproducciones;
}
