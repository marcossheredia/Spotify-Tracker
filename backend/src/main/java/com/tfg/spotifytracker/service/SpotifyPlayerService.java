package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.spotify.response.SpotifyActionResultDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyNowPlayingDTO;
import com.tfg.spotifytracker.dto.spotify.player.request.SpotifyPlaybackTransferRequestDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyPlayerDeviceDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyPlayerQueueDTO;
import com.tfg.spotifytracker.dto.spotify.player.response.SpotifyPlayerStateDTO;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
/**
 * Clase funcional: SpotifyPlayerService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifyApiClient, SpotifyDtoMapper.
 */
public class SpotifyPlayerService {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifyDtoMapper mapper;

    /** Obtiene datos para esta parte del sistema. */

    public SpotifyPlayerStateDTO getPlayerState(String accessToken) {
        try {
            Map<String, Object> state = spotifyApiClient.getMap(accessToken, "/me/player");
            if (state.isEmpty()) {
                return unavailableState("No hay reproduccion activa en este momento.");
            }

            Map<String, Object> item = mapper.asMap(state.get("item"));
            Map<String, Object> device = mapper.asMap(state.get("device"));
            List<SpotifyPlayerDeviceDTO> devices = getDevices(accessToken);

            SpotifyNowPlayingDTO currentTrack = mapper.toNowPlayingTrack(item, state);
            String product = resolveUserProduct(accessToken);
            boolean productKnown = product != null && !product.isBlank();
            boolean premium = "premium".equalsIgnoreCase(product);
            boolean hasActiveDevice = mapper.asString(device.get("id")) != null;
            boolean canControlPlayback = hasActiveDevice && (premium || !productKnown);

            return SpotifyPlayerStateDTO.builder()
                .available(true)
                .isPlaying(Boolean.TRUE.equals(mapper.asBoolean(state.get("is_playing"))))
                .progressMs(mapper.asNullableInteger(state.get("progress_ms")))
                .durationMs(mapper.asNullableInteger(item.get("duration_ms")))
                .timestamp(state.get("timestamp") instanceof Number n ? n.longValue() : null)
                .shuffleState(Boolean.TRUE.equals(mapper.asBoolean(state.get("shuffle_state"))))
                .repeatState(mapper.asString(state.get("repeat_state")))
                .currentlyPlayingType(mapper.asString(state.get("currently_playing_type")))
                .currentTrack(currentTrack)
                .activeDevice(toDevice(device))
                .devices(devices)
                .canControlPlayback(canControlPlayback)
                .requiresPremium(productKnown && !premium)
                .capabilitiesNote(resolveCapabilitiesNote(productKnown, premium, hasActiveDevice))
                .build();
        } catch (SpotifyApiException ex) {
            if (ex.getStatusCode() != null && ex.getStatusCode() == 204) {
                return unavailableState("No hay reproduccion activa en este momento.");
            }
            throw ex;
        }
    }

    /** Obtiene datos para esta parte del sistema. */

    public SpotifyPlayerQueueDTO getQueue(String accessToken) {
        Map<String, Object> queueResponse = spotifyApiClient.getMap(accessToken, "/me/player/queue");

        SpotifyNowPlayingDTO currentlyPlaying = mapper.toNowPlayingTrack(
            mapper.asMap(queueResponse.get("currently_playing")),
            Map.of()
        );

        List<SpotifyNowPlayingDTO> queue = new ArrayList<>();
        Object queueObj = queueResponse.get("queue");
        if (queueObj instanceof List<?> queueItems) {
            for (Object queueItem : queueItems) {
                SpotifyNowPlayingDTO dto = mapper.toNowPlayingTrack(mapper.asMap(queueItem), Map.of());
                if (dto != null) {
                    queue.add(dto);
                }
            }
        }

        return SpotifyPlayerQueueDTO.builder()
            .currentlyPlaying(currentlyPlaying)
            .queue(queue)
            .build();
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public SpotifyActionResultDTO play(String accessToken) {
        spotifyApiClient.putNoContent(accessToken, "/me/player/play", null);
        return ok("play", "Reproduccion reanudada");
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public SpotifyActionResultDTO pause(String accessToken) {
        spotifyApiClient.putNoContent(accessToken, "/me/player/pause", null);
        return ok("pause", "Reproduccion pausada");
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public SpotifyActionResultDTO next(String accessToken) {
        spotifyApiClient.postNoContent(accessToken, "/me/player/next", null);
        return ok("next", "Saltaste a la siguiente pista");
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public SpotifyActionResultDTO previous(String accessToken) {
        spotifyApiClient.postNoContent(accessToken, "/me/player/previous", null);
        return ok("previous", "Volviste a la pista anterior");
    }

    /** Actualiza una configuración o estado. */

    public SpotifyActionResultDTO setRepeat(String accessToken, String state) {
        String repeatState = state == null || state.isBlank() ? "off" : state;
        spotifyApiClient.putNoContent(accessToken, "/me/player/repeat?state=" + repeatState, null);
        return ok("repeat", "Modo repeat actualizado a " + repeatState);
    }

    /** Actualiza una configuración o estado. */

    public SpotifyActionResultDTO setShuffle(String accessToken, boolean enabled) {
        spotifyApiClient.putNoContent(accessToken, "/me/player/shuffle?state=" + enabled, null);
        return ok("shuffle", enabled ? "Shuffle activado" : "Shuffle desactivado");
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public SpotifyActionResultDTO transferPlayback(String accessToken, SpotifyPlaybackTransferRequestDTO request) {
        spotifyApiClient.putNoContent(
            accessToken,
            "/me/player",
            Map.of(
                "device_ids", List.of(request.getDeviceId()),
                "play", Boolean.TRUE.equals(request.getPlay())
            )
        );

        return ok("transfer", "Reproduccion transferida al dispositivo seleccionado");
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private SpotifyPlayerStateDTO unavailableState(String reason) {
        return SpotifyPlayerStateDTO.builder()
            .available(false)
            .reason(reason)
            .devices(List.of())
            .canControlPlayback(false)
            .requiresPremium(false)
            .capabilitiesNote(reason)
            .build();
    }

    /** Obtiene datos para esta parte del sistema. */

    private List<SpotifyPlayerDeviceDTO> getDevices(String accessToken) {
        Map<String, Object> response = spotifyApiClient.getMap(accessToken, "/me/player/devices");
        List<SpotifyPlayerDeviceDTO> devices = new ArrayList<>();

        Object devicesObj = response.get("devices");
        if (devicesObj instanceof List<?> deviceItems) {
            for (Object deviceItem : deviceItems) {
                SpotifyPlayerDeviceDTO dto = toDevice(mapper.asMap(deviceItem));
                if (dto != null) {
                    devices.add(dto);
                }
            }
        }

        return devices;
    }

    /** Transforma datos de un formato a otro. */

    private SpotifyPlayerDeviceDTO toDevice(Map<String, Object> device) {
        if (device == null || device.isEmpty()) {
            return null;
        }

        return SpotifyPlayerDeviceDTO.builder()
            .id(mapper.asString(device.get("id")))
            .name(mapper.asString(device.get("name")))
            .type(mapper.asString(device.get("type")))
            .active(Boolean.TRUE.equals(mapper.asBoolean(device.get("is_active"))))
            .volumePercent(mapper.asNullableInteger(device.get("volume_percent")))
            .restricted(Boolean.TRUE.equals(mapper.asBoolean(device.get("is_restricted"))))
            .build();
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private String resolveUserProduct(String accessToken) {
        Map<String, Object> me = spotifyApiClient.getMap(accessToken, "/me");
        return mapper.asString(me.get("product"));
    }

    /** Resuelve un valor final a partir del contexto actual. */

    private String resolveCapabilitiesNote(boolean productKnown, boolean premium, boolean hasActiveDevice) {
        if (!productKnown) {
            return hasActiveDevice
                ? "Spotify ya no devuelve el tipo de cuenta; algunas acciones pueden estar restringidas."
                : "No hay dispositivo activo para enviar controles remotos.";
        }

        if (premium) {
            return hasActiveDevice
                ? "Control remoto disponible para el dispositivo activo."
                : "No hay dispositivo activo para enviar controles remotos.";
        }

        return "Las acciones de control remoto requieren Spotify Premium.";
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private SpotifyActionResultDTO ok(String action, String message) {
        return SpotifyActionResultDTO.builder()
            .success(true)
            .action(action)
            .message(message)
            .build();
    }
}