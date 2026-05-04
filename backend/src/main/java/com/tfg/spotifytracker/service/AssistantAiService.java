package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;

import java.util.Optional;

public interface AssistantAiService {
    Optional<AssistantPlaylistPlanDTO> generatePlan(String message, Integer trackLimit, Boolean publicPlaylist);
}

