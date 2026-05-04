package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.assistant.request.AssistantPlaylistCreateRequestDTO;
import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistCreateResponseDTO;
import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import com.tfg.spotifytracker.dto.assistant.response.AssistantTrackDTO;
import com.tfg.spotifytracker.dto.spotify.search.response.SpotifySearchResultDTO;
import com.tfg.spotifytracker.dto.spotify.common.SpotifyTrackDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.exception.ResourceNotFoundException;
import com.tfg.spotifytracker.exception.SpotifyApiException;
import com.tfg.spotifytracker.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AssistantPlaylistService {

    private static final int SEARCH_LIMIT = 10;
    private static final int TRACKS_PER_ADD_REQUEST = 100;

    private final SpotifyTokenService spotifyTokenService;
    private final SpotifyApiClient spotifyApiClient;
    private final SpotifySearchService spotifySearchService;
    private final SpotifyDtoMapper spotifyDtoMapper;
    private final AssistantPlaylistPlannerService assistantPlaylistPlannerService;

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

        Map<String, Object> me = spotifyApiClient.getMap(accessToken, "/me");
        String userId = spotifyDtoMapper.asString(me.get("id"));
        if (!StringUtils.hasText(userId)) {
            throw new SpotifyApiException("No se pudo identificar el usuario de Spotify");
        }

        Map<String, Object> playlistResponse = spotifyApiClient.postMap(
            accessToken,
            "/me/playlists",
            Map.of(
                "name", plan.getPlaylistName(),
                "description", plan.getDescription(),
                "public", Boolean.TRUE.equals(plan.getPublicPlaylist())
            )
        );

        String playlistId = spotifyDtoMapper.asString(playlistResponse.get("id"));
        if (!StringUtils.hasText(playlistId)) {
            throw new SpotifyApiException("Spotify no devolvio un id de playlist");
        }

        List<String> uris = tracks.stream()
            .map(AssistantTrackDTO::getUri)
            .filter(StringUtils::hasText)
            .toList();

        if (!uris.isEmpty()) {
            addTracks(accessToken, playlistId, uris);
        }

        String playlistName = spotifyDtoMapper.asString(playlistResponse.get("name"));
        String externalUrl = spotifyDtoMapper.asString(
            spotifyDtoMapper.asMap(playlistResponse.get("external_urls")).get("spotify")
        );

        String message = buildAssistantMessage(playlistName, uris.size());

        return AssistantPlaylistCreateResponseDTO.builder()
            .playlistId(playlistId)
            .playlistName(playlistName)
            .externalUrl(externalUrl)
            .tracksAdded(uris.size())
            .message(message)
            .tracks(tracks)
            .build();
    }

    private List<AssistantTrackDTO> findTracks(String accessToken, AssistantPlaylistPlanDTO plan) {
        int targetCount = plan.getTrackLimit() == null ? 0 : plan.getTrackLimit();
        List<String> searchQueries = plan.getSearchQueries() == null ? List.of() : plan.getSearchQueries();

        List<AssistantTrackDTO> tracks = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String query : searchQueries) {
            if (tracks.size() >= targetCount) {
                break;
            }

            if (!StringUtils.hasText(query)) {
                continue;
            }

            SpotifySearchResultDTO searchResult = spotifySearchService.search(
                accessToken,
                query,
                "track",
                SEARCH_LIMIT,
                0
            );

            for (SpotifyTrackDTO track : searchResult.getTracks()) {
                if (track == null || !StringUtils.hasText(track.getId())) {
                    continue;
                }
                if (!seen.add(track.getId())) {
                    continue;
                }

                tracks.add(toAssistantTrack(track));
                if (tracks.size() >= targetCount) {
                    break;
                }
            }
        }

        return tracks;
    }

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

    private String buildAssistantMessage(String playlistName, int tracksAdded) {
        String safeName = StringUtils.hasText(playlistName) ? playlistName : "tu playlist";
        return "He creado tu playlist '" + safeName + "' con " + tracksAdded + " canciones.";
    }
}

