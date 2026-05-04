package com.tfg.spotifytracker.dto.spotify.playlist.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpotifyPlaylistAutomationResponseDTO {
    private String playlistId;
    private String playlistName;
    private String externalUrl;
    private Integer tracksAdded;
}

