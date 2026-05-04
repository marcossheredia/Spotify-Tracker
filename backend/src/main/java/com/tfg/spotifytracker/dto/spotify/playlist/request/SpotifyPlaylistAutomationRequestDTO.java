package com.tfg.spotifytracker.dto.spotify.playlist.request;

import lombok.Data;

@Data
public class SpotifyPlaylistAutomationRequestDTO {
    private String name;
    private String description;
    private String timeRange;
    private Integer limit;
    private Boolean publicPlaylist;
}
