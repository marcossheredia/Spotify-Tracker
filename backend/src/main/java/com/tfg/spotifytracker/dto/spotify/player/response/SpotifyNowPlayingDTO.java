package com.tfg.spotifytracker.dto.spotify.player.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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