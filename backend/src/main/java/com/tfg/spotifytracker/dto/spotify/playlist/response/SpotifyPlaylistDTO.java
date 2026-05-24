package com.tfg.spotifytracker.dto.spotify.playlist.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: SpotifyPlaylistDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPlaylistDTO {
    private String id;
    private String name;
    private String imageUrl;
    private Integer tracksTotal;
    private Boolean publicPlaylist;
    private String ownerName;
    private String ownerId;
    private String externalUrl;
    private String lastPlayedAt;
    private Boolean ownPlaylist;
    private Boolean collaborative;
    private Boolean hasLikedTracks;
}
