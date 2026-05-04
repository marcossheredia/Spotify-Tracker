package com.tfg.spotifytracker.dto.spotify.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpotifyActionResultDTO {
    private Boolean success;
    private String action;
    private String message;
    private Boolean requiresPremium;
    private Boolean requiresActiveDevice;
    private Integer retryAfterSeconds;
}

