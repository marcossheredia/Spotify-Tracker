package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.assistant.request.AssistantPlaylistCreateRequestDTO;
import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistCreateResponseDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.service.AssistantPlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assistant", description = "Asistente IA para playlists")
@Validated
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
/**
 * Clase funcional: AssistantController.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: AssistantPlaylistService.
 */
public class AssistantController {

    private final AssistantPlaylistService assistantPlaylistService;

    @Operation(summary = "Crear playlist desde lenguaje natural")
    @PostMapping("/playlists/create")
    public ResponseEntity<AssistantPlaylistCreateResponseDTO> createPlaylist(
        @AuthenticationPrincipal Usuario usuario,
        @Valid @RequestBody AssistantPlaylistCreateRequestDTO request
    ) {
        return ResponseEntity.ok(assistantPlaylistService.createPlaylist(usuario, request));
    }
}
