package com.tfg.spotifytracker.dto.spotify.profile.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: SpotifyUserProfileDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyUserProfileDTO {
    private String spotifyId;
    private String displayName;
    private String email;
    private String imageUrl;
    private String country;
    private String product;
    private String externalUrl;
    private Integer followersTotal;
    private Boolean premiumCapabilitiesAvailable;
    private String capabilitiesNote;
}
