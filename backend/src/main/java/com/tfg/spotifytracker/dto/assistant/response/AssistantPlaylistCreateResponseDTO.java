package com.tfg.spotifytracker.dto.assistant.response;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: AssistantPlaylistCreateResponseDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class AssistantPlaylistCreateResponseDTO {
    private String playlistId;
    private String playlistName;
    private String externalUrl;
    private Integer tracksAdded;
    private String message;
    private List<AssistantTrackDTO> tracks;
}