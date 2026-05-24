package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifyNowPlayingDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyNowPlayingDTO {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> artists;
    private String albumName;
    private String externalUrl;
    private Boolean isPlaying;
    private Integer progressMs;
    private Integer durationMs;
}