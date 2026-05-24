package com.tfg.spotifytracker.dto.spotify.playlist.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifyPlaylistTrackDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlaylistTrackDTO {
    private String id;
    private String name;
    private List<String> artists;
    private String albumName;
    private Integer durationMs;
    private String externalUrl;
    private Boolean liked;
}
