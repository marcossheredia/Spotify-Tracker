package com.tfg.spotifytracker.dto.spotify.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifyCompatibilityStatusDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyCompatibilityStatusDTO {
    private String spotifyUserId;
    private String product;
    private boolean canReadProfile;
    private boolean canReadTopItems;
    private boolean canReadLibrary;
    private boolean canModifyLibrary;
    private boolean canCreatePlaylist;
    private boolean canControlPlayback;
    private List<String> warnings;
}

