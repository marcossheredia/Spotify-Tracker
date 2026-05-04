package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpotifyPlayerDeviceDTO {
    private String id;
    private String name;
    private String type;
    private Boolean active;
    private Integer volumePercent;
    private Boolean restricted;
}
