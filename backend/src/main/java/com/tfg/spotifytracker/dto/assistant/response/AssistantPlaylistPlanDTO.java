package com.tfg.spotifytracker.dto.assistant.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
/**
 * Clase funcional: AssistantPlaylistPlanDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class AssistantPlaylistPlanDTO {
    private String originalMessage;
    private String playlistName;
    private String description;
    private String intent;
    private String language;
    private String eventContext;
    private String targetAudience;
    private String preferredMarket;
    private String energyLevel;
    private String primaryGenre;
    private Integer yearStart;
    private Integer yearEnd;
    private Integer targetDurationMinutes;
    private Boolean avoidExplicit;
    private List<String> contextTags;
    private List<String> moods;
    private List<String> genres;
    private List<String> secondaryGenres;
    private List<String> negativeConstraints;
    private List<String> searchQueries;
    private Integer trackLimit;
    private Boolean publicPlaylist;
}
