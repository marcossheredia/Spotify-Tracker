package com.tfg.spotifytracker.dto.assistant.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AssistantPlaylistPlanDTO {
    private String originalMessage;
    private String playlistName;
    private String description;
    private String intent;
    private String language;
    private List<String> contextTags;
    private List<String> genres;
    private List<String> searchQueries;
    private Integer trackLimit;
    private Boolean publicPlaylist;
}
