package com.tfg.spotifytracker.dto.spotify.player.request;

import lombok.Data;

@Data
public class SpotifyPlaybackSettingsRequestDTO {
    private String repeatState;
    private Boolean shuffleState;
}

