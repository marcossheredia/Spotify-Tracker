package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.config.AssistantAiProperties;
import com.tfg.spotifytracker.dto.assistant.response.AssistantStatusDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assistant", description = "Estado del proveedor de IA")
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantStatusController {

    private final AssistantAiProperties assistantAiProperties;

    @Operation(summary = "Estado del proveedor IA del asistente")
    @GetMapping("/status")
    public ResponseEntity<AssistantStatusDTO> getStatus(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        String provider = assistantAiProperties.getProvider();
        boolean configured = "mock".equalsIgnoreCase(provider);

        return ResponseEntity.ok(AssistantStatusDTO.builder()
            .provider(provider)
            .model(null)
            .configured(configured)
            .fallbackAvailable(true)
            .build());
    }
}
