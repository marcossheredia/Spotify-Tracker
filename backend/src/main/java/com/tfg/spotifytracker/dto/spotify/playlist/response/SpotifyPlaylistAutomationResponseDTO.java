package com.tfg.spotifytracker.dto.spotify.playlist.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: SpotifyPlaylistAutomationResponseDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlaylistAutomationResponseDTO {
    private String playlistId;
    private String playlistName;
    private String externalUrl;
    private Integer tracksAdded;
    private String timeRange;
}
