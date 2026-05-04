package com.tfg.spotifytracker.dto.spotify.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotifyArtistDTO {
    private String id;
    private String name;
    private String imageUrl;
    private Integer followersTotal;
    private List<String> genres;
    private String externalUrl;
    private Integer popularity;
}