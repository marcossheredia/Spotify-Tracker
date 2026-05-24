package com.tfg.spotifytracker.dto.spotify.playlist.request;

import lombok.Data;

@Data
/**
 * Clase funcional: SpotifyPlaylistAutomationRequestDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlaylistAutomationRequestDTO {
    private String name;
    private String description;
    private String timeRange;
    private Integer limit;
    private Boolean publicPlaylist;
}
