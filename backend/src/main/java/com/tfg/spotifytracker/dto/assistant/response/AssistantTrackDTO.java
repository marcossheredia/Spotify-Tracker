package com.tfg.spotifytracker.dto.assistant.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssistantTrackDTO {
    private String id;
    private String name;
    private String artist;
    private String uri;
}
