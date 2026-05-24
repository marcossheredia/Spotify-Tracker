package com.tfg.spotifytracker.dto.assistant.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: AssistantTrackDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class AssistantTrackDTO {
    private String id;
    private String name;
    private String artist;
    private String uri;
}
