package com.tfg.spotifytracker.dto.spotify.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotifyAlbumDTO {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> artists;
    private String albumType;
    private Integer totalTracks;
    private String releaseDate;
    private String externalUrl;
}