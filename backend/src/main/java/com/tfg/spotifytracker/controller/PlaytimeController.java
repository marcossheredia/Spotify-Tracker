package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.playback.response.PlaytimeHistoryResponseDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.RecentPlaybackSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Playtime", description = "Historico de tiempo escuchado")
@RestController
@RequestMapping("/api/playtime")
@RequiredArgsConstructor
/**
 * Clase funcional: PlaytimeController.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: RecentPlaybackSyncService.
 */
public class PlaytimeController {

    private final RecentPlaybackSyncService recentPlaybackSyncService;

    @Operation(summary = "Obtener historico de escucha agrupado por dia/semana/mes")
    @GetMapping("/history")
    public ResponseEntity<PlaytimeHistoryResponseDTO> getHistory(
        @AuthenticationPrincipal Usuario usuario,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(defaultValue = "day") String frequency
    ) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }
        return ResponseEntity.ok(recentPlaybackSyncService.getPlaytimeHistoryByDate(usuario, from, to, frequency));
    }
}

