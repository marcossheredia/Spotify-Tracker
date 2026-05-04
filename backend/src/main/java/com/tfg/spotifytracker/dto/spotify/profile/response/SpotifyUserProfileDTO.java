package com.tfg.spotifytracker.dto.spotify.profile.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
