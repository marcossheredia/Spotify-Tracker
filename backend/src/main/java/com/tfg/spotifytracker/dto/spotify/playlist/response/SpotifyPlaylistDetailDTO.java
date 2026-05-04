package com.tfg.spotifytracker.dto.spotify.playlist.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpotifyPlaylistDetailDTO {
    private String id;
    private String name;
    private String imageUrl;
    private Integer tracksTotal;
    private String ownerName;
    private String externalUrl;
    private String lastPlayedAt;
    private Boolean ownPlaylist;
    private Boolean collaborative;
    private Boolean hasLikedTracks;
    private Boolean canLoadTracks;
    private String unavailableReason;
    private List<SpotifyPlaylistTrackDTO> tracks;
}

