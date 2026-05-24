package com.tfg.spotifytracker.dto.spotify.library.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifySavedAlbumDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
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

