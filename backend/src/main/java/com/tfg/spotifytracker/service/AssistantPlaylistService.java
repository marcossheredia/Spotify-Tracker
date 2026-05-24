package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.assistant.request.AssistantPlaylistCreateRequestDTO;
import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistCreateResponseDTO;
import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import com.tfg.spotifytracker.dto.assistant.response.AssistantTrackDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.dto.spotify.search.response.SpotifySearchResultDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.ResourceNotFoundException;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
/**
 * Clase funcional: AssistantPlaylistService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: SpotifyTokenService, SpotifyApiClient, SpotifySearchService, SpotifyDtoMapper, AssistantPlaylistPlannerService.
 */
public class AssistantPlaylistService {

    private static final int SEARCH_LIMIT = 10;
    private static final int MAX_QUERIES_PER_REQUEST = 1;
    private static final int TRACKS_PER_ADD_REQUEST = 100;
    private static volatile Instant spotifySearchCooldownUntil = Instant.EPOCH;

    private final SpotifyTokenService spotifyTokenService;
    private final SpotifyApiClient spotifyApiClient;
    private final SpotifySearchService spotifySearchService;
    private final SpotifyDtoMapper spotifyDtoMapper;
    private final AssistantPlaylistPlannerService assistantPlaylistPlannerService;

    /** Crea un recurso nuevo con los datos recibidos. */

    public AssistantPlaylistCreateResponseDTO createPlaylist(Usuario usuario, AssistantPlaylistCreateRequestDTO request) {
        if (usuario == null) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        AssistantPlaylistPlanDTO plan = assistantPlaylistPlannerService.buildPlan(
            request.getMessage(),
            request.getTrackLimit(),
            request.getPublicPlaylist()
        );

        List<AssistantTrackDTO> tracks = findTracks(accessToken, plan);
        if (tracks.isEmpty()) {
            throw new ResourceNotFoundException("No se han encontrado canciones para tu solicitud.");
        }

        Map<String, Object> playlistBody = new HashMap<>();
        playlistBody.put("name", safePlaylistName(plan));
        playlistBody.put("description", safeDescription(plan));
        playlistBody.put("public", Boolean.TRUE.equals(plan.getPublicPlaylist()));

        Map<String, Object> playlistResponse = spotifyApiClient.postMap(
            accessToken,
            "/me/playlists",
            playlistBody
        );

        String playlistId = spotifyDtoMapper.asString(playlistResponse.get("id"));
        if (!StringUtils.hasText(playlistId)) {
            throw new SpotifyApiException("Spotify no devolvio un id de playlist");
        }

        List<String> uris = tracks.stream()
            .map(AssistantTrackDTO::getUri)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();

        if (uris.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron URIs validas para crear la playlist.");
        }

        addTracks(accessToken, playlistId, uris);

        String playlistName = spotifyDtoMapper.asString(playlistResponse.get("name"));
        String externalUrl = spotifyDtoMapper.asString(
            spotifyDtoMapper.asMap(playlistResponse.get("external_urls")).get("spotify")
        );

        return AssistantPlaylistCreateResponseDTO.builder()
            .playlistId(playlistId)
            .playlistName(StringUtils.hasText(playlistName) ? playlistName : safePlaylistName(plan))
            .externalUrl(externalUrl)
            .tracksAdded(uris.size())
            .message(buildAssistantMessage(playlistName, uris.size()))
            .tracks(tracks)
            .build();
    }

    /** Busca un dato concreto para poder usarlo en el flujo. */

    private List<AssistantTrackDTO> findTracks(String accessToken, AssistantPlaylistPlanDTO plan) {
        if (spotifySearchCooldownUntil.isAfter(Instant.now())) {
            throw new SpotifyApiException(
                "Spotify aplico rate limit. Espera unos segundos antes de volver a crear playlists.",
                429,
                Math.max(1, (int) (spotifySearchCooldownUntil.getEpochSecond() - Instant.now().getEpochSecond())),
                "RATE_LIMITED",
                null
            );
        }

        int targetCount = Math.max(5, Math.min(plan.getTrackLimit() == null ? 25 : plan.getTrackLimit(), 25));
        List<String> searchQueries = plan.getSearchQueries() == null ? List.of() : plan.getSearchQueries();

        List<AssistantTrackDTO> tracks = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int queriesUsed = 0;

        for (String query : searchQueries) {
            if (tracks.size() >= targetCount || queriesUsed >= MAX_QUERIES_PER_REQUEST) {
                break;
            }
            if (!StringUtils.hasText(query)) {
                continue;
            }

            queriesUsed++;
            SpotifySearchResultDTO searchResult;
            try {
                searchResult = spotifySearchService.search(accessToken, query, "track", SEARCH_LIMIT, 0);
            } catch (SpotifyApiException ex) {
                if (ex.getStatusCode() != null && ex.getStatusCode() == 429) {
                    int retryAfter = ex.getRetryAfterSeconds() == null ? 30 : Math.max(10, ex.getRetryAfterSeconds());
                    spotifySearchCooldownUntil = Instant.now().plusSeconds(retryAfter);
                }
                throw ex;
            }

            List<SpotifyTrackDTO> spotifyTracks = searchResult.getTracks() == null ? List.of() : searchResult.getTracks();
            for (SpotifyTrackDTO track : spotifyTracks) {
                if (track == null || !StringUtils.hasText(track.getId()) || !seen.add(track.getId())) {
                    continue;
                }
                AssistantTrackDTO assistantTrack = toAssistantTrack(track);
                if (StringUtils.hasText(assistantTrack.getUri())) {
                    tracks.add(assistantTrack);
                }
                if (tracks.size() >= targetCount) {
                    break;
                }
            }
        }

        return tracks;
    }

    /** Transforma datos de un formato a otro. */

    private AssistantTrackDTO toAssistantTrack(SpotifyTrackDTO track) {
        String artist = track.getArtists() != null && !track.getArtists().isEmpty()
            ? String.join(", ", track.getArtists())
            : "Artista desconocido";

        return AssistantTrackDTO.builder()
            .id(track.getId())
            .name(track.getName())
            .artist(artist)
            .uri(StringUtils.hasText(track.getId()) ? "spotify:track:" + track.getId() : null)
            .build();
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private void addTracks(String accessToken, String playlistId, List<String> uris) {
        int total = uris.size();
        int start = 0;

        while (start < total) {
            int end = Math.min(start + TRACKS_PER_ADD_REQUEST, total);
            List<String> batch = uris.subList(start, end);

            spotifyApiClient.postNoContent(
                accessToken,
                "/playlists/" + playlistId + "/items",
                Map.of("uris", batch)
            );

            start = end;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private String safePlaylistName(AssistantPlaylistPlanDTO plan) {
        return StringUtils.hasText(plan.getPlaylistName()) ? plan.getPlaylistName() : "Playlist personalizada";
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private String safeDescription(AssistantPlaylistPlanDTO plan) {
        return StringUtils.hasText(plan.getDescription())
            ? plan.getDescription()
            : "Playlist creada por Spotify Tracker.";
    }

    /** Construye una respuesta o estructura intermedia. */

    private String buildAssistantMessage(String playlistName, int tracksAdded) {
        String safeName = StringUtils.hasText(playlistName) ? playlistName : "tu playlist";
        return "He creado tu playlist '" + safeName + "' con " + tracksAdded + " canciones.";
    }
}
