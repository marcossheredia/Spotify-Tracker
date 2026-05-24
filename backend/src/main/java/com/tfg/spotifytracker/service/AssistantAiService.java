package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;

import java.util.Optional;

/**
 * Clase funcional: AssistantAiService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras partes de la aplicación.
 */
public interface AssistantAiService {
    Optional<AssistantPlaylistPlanDTO> generatePlan(String message, Integer trackLimit, Boolean publicPlaylist);
}

