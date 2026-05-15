package com.tfg.spotifytracker.dto.spotify.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotifyTrackDTO {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> artists;
    private String albumName;
    private String releaseDate;
    private Integer releaseYear;
    private Integer durationMs;
    private Boolean explicit;
    private String externalUrl;
    private Integer popularity;
}
