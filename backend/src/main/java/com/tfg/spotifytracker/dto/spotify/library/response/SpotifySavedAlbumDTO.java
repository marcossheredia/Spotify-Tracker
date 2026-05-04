package com.tfg.spotifytracker.dto.spotify.library.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotifySavedAlbumDTO {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> artists;
    private String albumType;
    private Integer totalTracks;
    private String releaseDate;
    private String externalUrl;
    private String addedAt;
}

