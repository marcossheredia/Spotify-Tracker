package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyActionResultDTO;
import com.tfg.spotifytracker.dto.spotify.player.request.SpotifyPlaybackSettingsRequestDTO;
import com.tfg.spotifytracker.dto.spotify.player.request.SpotifyPlaybackTransferRequestDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyPlayerQueueDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyPlayerStateDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import com.tfg.spotifytracker.service.SpotifyPlayerService;
import com.tfg.spotifytracker.service.SpotifyTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Tag(name = "Spotify Player", description = "Estado y controles de reproduccion")
@RestController
@RequestMapping("/api/spotify/player")
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifyPlayerController.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifyPlayerService, SpotifyTokenService.
 */
public class SpotifyPlayerController {

    private final SpotifyPlayerService spotifyPlayerService;
    private final SpotifyTokenService spotifyTokenService;

    @Operation(summary = "Obtener estado completo del reproductor")
    @GetMapping("/state")
    /** Obtiene datos para esta parte del sistema. */
    public ResponseEntity<SpotifyPlayerStateDTO> getPlayerState(@AuthenticationPrincipal Usuario usuario) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyPlayerService.getPlayerState(accessToken));
    }

    @Operation(summary = "Obtener cola actual")
    @GetMapping("/queue")
    /** Obtiene datos para esta parte del sistema. */
    public ResponseEntity<SpotifyPlayerQueueDTO> getQueue(@AuthenticationPrincipal Usuario usuario) {
        String accessToken = resolveAccessToken(usuario);
        return ResponseEntity.ok(spotifyPlayerService.getQueue(accessToken));
    }

    @Operation(summary = "Reanudar reproduccion")
    @PostMapping("/play")
    /** Ejecuta una parte concreta de la lógica de esta clase. */
    public ResponseEntity<SpotifyActionResultDTO> play(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(spotifyPlayerService.play(resolveAccessToken(usuario)));
    }

    @Operation(summary = "Pausar reproduccion")
    @PostMapping("/pause")
    /** Ejecuta una parte concreta de la lógica de esta clase. */
    public ResponseEntity<SpotifyActionResultDTO> pause(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(spotifyPlayerService.pause(resolveAccessToken(usuario)));
    }

    @Operation(summary = "Siguiente pista")
    @PostMapping("/next")
    /** Ejecuta una parte concreta de la lógica de esta clase. */
    public ResponseEntity<SpotifyActionResultDTO> next(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(spotifyPlayerService.next(resolveAccessToken(usuario)));
    }

    @Operation(summary = "Pista anterior")
    @PostMapping("/previous")
    /** Ejecuta una parte concreta de la lógica de esta clase. */
    public ResponseEntity<SpotifyActionResultDTO> previous(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(spotifyPlayerService.previous(resolveAccessToken(usuario)));
    }

    @Operation(summary = "Actualizar repeat/shuffle")
    @PostMapping("/settings")
    public ResponseEntity<SpotifyActionResultDTO> updateSettings(
        @AuthenticationPrincipal Usuario usuario,
        @RequestBody SpotifyPlaybackSettingsRequestDTO request
    ) {
        String accessToken = resolveAccessToken(usuario);

        if (request.getRepeatState() != null) {
            return ResponseEntity.ok(spotifyPlayerService.setRepeat(accessToken, request.getRepeatState()));
        }

        if (request.getShuffleState() != null) {
            return ResponseEntity.ok(spotifyPlayerService.setShuffle(accessToken, request.getShuffleState()));
        }

        return ResponseEntity.ok(SpotifyActionResultDTO.builder()
            .success(false)
            .action("settings")
            .message("No se recibieron cambios de repeat o shuffle")
            .build());
    }

    @Operation(summary = "Transferir reproduccion")
    @PostMapping("/transfer")
    public ResponseEntity<SpotifyActionResultDTO> transfer(
        @AuthenticationPrincipal Usuario usuario,
        @RequestBody SpotifyPlaybackTransferRequestDTO request
    ) {
        if (request == null || !StringUtils.hasText(request.getDeviceId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Debes indicar un deviceId valido para transferir reproduccion");
        }

        return ResponseEntity.ok(spotifyPlayerService.transferPlayback(resolveAccessToken(usuario), request));
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private String resolveAccessToken(Usuario usuario) {
        if (usuario == null || !StringUtils.hasText(usuario.getAccessToken())) {
            throw new UnauthorizedException("Usuario no autenticado en Spotify");
        }

        return spotifyTokenService.getValidAccessToken(usuario);
    }
}

