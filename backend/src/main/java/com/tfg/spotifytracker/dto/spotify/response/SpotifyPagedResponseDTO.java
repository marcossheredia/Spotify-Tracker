package com.tfg.spotifytracker.dto.spotify.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: SpotifyPagedResponseDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class SpotifyPagedResponseDTO<T> {
    private List<T> items;
    private Integer limit;
    private Integer offset;
    private Integer total;
    private String nextCursor;
    private Boolean hasNext;
}

