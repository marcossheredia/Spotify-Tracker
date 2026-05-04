package com.tfg.spotifytracker.dto.assistant.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssistantStatusDTO {
    private String provider;
    private String model;
    private boolean configured;
    private boolean fallbackAvailable;
}
