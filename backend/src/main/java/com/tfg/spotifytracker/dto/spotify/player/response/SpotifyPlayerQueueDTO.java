package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifyPlayerQueueDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlayerQueueDTO {
    private SpotifyNowPlayingDTO currentlyPlaying;
    private List<SpotifyNowPlayingDTO> queue;
}
