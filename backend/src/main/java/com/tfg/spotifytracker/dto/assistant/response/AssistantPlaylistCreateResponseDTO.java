package com.tfg.spotifytracker.dto.assistant.response;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AssistantPlaylistCreateResponseDTO {
    private String playlistId;
    private String playlistName;
    private String externalUrl;
    private Integer tracksAdded;
    private String message;
    private List<AssistantTrackDTO> tracks;
}