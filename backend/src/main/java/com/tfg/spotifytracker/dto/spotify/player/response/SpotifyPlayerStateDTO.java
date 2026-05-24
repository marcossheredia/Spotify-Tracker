package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifyPlayerStateDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlayerStateDTO {
    private Boolean available;
    private String reason;
    private Boolean isPlaying;
    private Integer progressMs;
    private Integer durationMs;
    private Long timestamp;
    private Boolean shuffleState;
    private String repeatState;
    private String currentlyPlayingType;
    private SpotifyNowPlayingDTO currentTrack;
    private SpotifyPlayerDeviceDTO activeDevice;
    private List<SpotifyPlayerDeviceDTO> devices;
    private Boolean canControlPlayback;
    private Boolean requiresPremium;
    private String capabilitiesNote;
}