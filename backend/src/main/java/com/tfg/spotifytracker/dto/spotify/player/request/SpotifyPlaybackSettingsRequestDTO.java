package com.tfg.spotifytracker.dto.spotify.player.request;

import lombok.Data;

@Data
/**
 * Clase funcional: SpotifyPlaybackSettingsRequestDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlaybackSettingsRequestDTO {
    private String repeatState;
    private Boolean shuffleState;
}

