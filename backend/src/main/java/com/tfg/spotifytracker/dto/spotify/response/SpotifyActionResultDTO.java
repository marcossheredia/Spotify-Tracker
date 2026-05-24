package com.tfg.spotifytracker.dto.spotify.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: SpotifyActionResultDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyActionResultDTO {
    private Boolean success;
    private String action;
    private String message;
    private Boolean requiresPremium;
    private Boolean requiresActiveDevice;
    private Integer retryAfterSeconds;
}

