package com.tfg.spotifytracker.dto.spotify.player.request;

import lombok.Data;

@Data
public class SpotifyPlaybackTransferRequestDTO {
    private String deviceId;
    private Boolean play;
}