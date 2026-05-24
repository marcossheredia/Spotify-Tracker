package com.tfg.spotifytracker.dto.assistant.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: AssistantStatusDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class AssistantStatusDTO {
    private String provider;
    private String model;
    private boolean configured;
    private boolean fallbackAvailable;
}
