package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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