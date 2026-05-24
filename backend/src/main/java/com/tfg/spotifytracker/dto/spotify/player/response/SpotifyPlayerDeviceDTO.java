package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: SpotifyPlayerDeviceDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlayerDeviceDTO {
    private String id;
    private String name;
    private String type;
    private Boolean active;
    private Integer volumePercent;
    private Boolean restricted;
}
