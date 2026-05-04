package com.tfg.spotifytracker.dto.spotify.library.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotifySavedTrackDTO {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> artists;
    private String albumName;
    private String externalUrl;
    private Integer durationMs;
    private String addedAt;
}
