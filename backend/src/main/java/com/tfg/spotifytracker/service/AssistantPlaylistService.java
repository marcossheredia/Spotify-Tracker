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
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssistantPlaylistService {

    private static final int SEARCH_LIMIT = 25;
    private static final int SEARCH_PAGES = 1;
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
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new ResourceNotFoundException("Debes indicar una solicitud para crear la playlist.");
        }

        String accessToken = spotifyTokenService.getValidAccessToken(usuario);
        if (!StringUtils.hasText(accessToken)) {
            throw new UnauthorizedException("No hay token valido de Spotify para este usuario.");
        }

        AssistantPlaylistPlanDTO plan = assistantPlaylistPlannerService.buildPlan(
            request.getMessage(),
            request.getTrackLimit(),
            request.getPublicPlaylist()
        );
        if (plan == null) {
            throw new ResourceNotFoundException("No se pudo interpretar tu solicitud musical.");
        }
        if (!StringUtils.hasText(plan.getPlaylistName())) {
            plan.setPlaylistName("Playlist personalizada");
        }
        if (!StringUtils.hasText(plan.getDescription())) {
            plan.setDescription("Playlist creada por Spotify Tracker");
        }
        if (plan.getSearchQueries() == null || plan.getSearchQueries().isEmpty()) {
            throw new ResourceNotFoundException("No se pudieron generar consultas musicales para crear la playlist.");
        }

        log.info("Assistant playlist plan: name='{}', queries={}, targetDurationMin={}, trackLimit={}",
            plan.getPlaylistName(),
            plan.getSearchQueries() == null ? 0 : plan.getSearchQueries().size(),
            plan.getTargetDurationMinutes(),
            plan.getTrackLimit());
        log.info("Assistant request userId={} prompt='{}' publicPlaylist={}",
            usuario.getId(), request.getMessage(), Boolean.TRUE.equals(request.getPublicPlaylist()));

        List<AssistantTrackDTO> tracks = findTracks(accessToken, plan);
        log.info("Assistant playlist selected tracks={}", tracks.size());
        if (tracks.isEmpty()) {
            throw new ResourceNotFoundException("No se han encontrado canciones para tu solicitud.");
        }

        Map<String, Object> me = spotifyApiClient.getMap(accessToken, "/me");
        String userId = spotifyDtoMapper.asString(me.get("id"));
        if (!StringUtils.hasText(userId)) {
            throw new SpotifyApiException("No se pudo identificar el usuario de Spotify");
        }

        Map<String, Object> playlistResponse;
        String createPlaylistEndpoint = "/me/playlists";
        Map<String, Object> createPlaylistBody = new LinkedHashMap<>();
        createPlaylistBody.put("name", plan.getPlaylistName());
        createPlaylistBody.put("description", plan.getDescription());
        createPlaylistBody.put("public", Boolean.TRUE.equals(plan.getPublicPlaylist()));
        try {
            log.info("Creating playlist in Spotify endpoint={} name='{}' spotifyUserId={}",
                createPlaylistEndpoint, plan.getPlaylistName(), userId);
            playlistResponse = spotifyApiClient.postMap(
                accessToken,
                createPlaylistEndpoint,
                createPlaylistBody
            );
        } catch (SpotifyApiException ex) {
            log.warn("Assistant playlist creation failed endpoint={} name='{}' status={}",
                createPlaylistEndpoint, plan.getPlaylistName(), ex.getStatusCode());
            throw mapPlaylistCreationError(ex);
        }

        String playlistId = spotifyDtoMapper.asString(playlistResponse.get("id"));
        if (!StringUtils.hasText(playlistId)) {
            throw new SpotifyApiException("Spotify no devolvio un id de playlist");
        }

        List<String> uris = tracks.stream()
            .map(AssistantTrackDTO::getUri)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();

        if (uris.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron canciones para crear la playlist.");
        }

        try {
            addTracks(accessToken, playlistId, uris);
        } catch (SpotifyApiException ex) {
            throw mapPlaylistAddTracksError(ex);
        }

        String playlistName = spotifyDtoMapper.asString(playlistResponse.get("name"));
        String externalUrl = spotifyDtoMapper.asString(
            spotifyDtoMapper.asMap(playlistResponse.get("external_urls")).get("spotify")
        );

        String message = buildAssistantMessage(playlistName, uris.size(), plan.getTargetDurationMinutes(), tracks);

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
        List<String> searchQueries = plan.getSearchQueries() == null ? new ArrayList<>() : new ArrayList<>(plan.getSearchQueries());
        if (searchQueries.isEmpty()) {
            searchQueries.addAll(buildEmergencyQueries(plan));
        }
        searchQueries = new ArrayList<>(new LinkedHashSet<>(searchQueries.stream().filter(StringUtils::hasText).map(String::trim).toList()));
        if (searchQueries.size() > 3) {
            searchQueries = new ArrayList<>(searchQueries.subList(0, 3));
        }

        int targetByCount = plan.getTrackLimit() == null ? 25 : plan.getTrackLimit();
        Integer targetDurationMinutes = plan.getTargetDurationMinutes();
        int minTracksToCreate = 5;
        int effectiveTarget = Math.max(minTracksToCreate, targetByCount);

        List<ScoredTrack> candidates = new ArrayList<>();
        List<SpotifyTrackDTO> fallbackPool = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        boolean rateLimited = false;

        for (String query : searchQueries) {
            for (int page = 0; page < SEARCH_PAGES; page++) {
                if (fallbackPool.size() >= effectiveTarget) {
                    break;
                }
                SpotifySearchResultDTO searchResult;
                try {
                    searchResult = spotifySearchService.search(
                        accessToken,
                        query,
                        "track",
                        SEARCH_LIMIT,
                        page * SEARCH_LIMIT
                    );
                } catch (SpotifyApiException ex) {
                    if (ex.getStatusCode() != null && ex.getStatusCode() == 429) {
                        rateLimited = true;
                        break;
                    }
                    if (ex.getStatusCode() != null && (ex.getStatusCode() == 400 || ex.getStatusCode() == 403)) {
                        log.warn("Assistant search query skipped: query='{}' offset={} status={}", query, page * SEARCH_LIMIT, ex.getStatusCode());
                        break;
                    }
                    throw ex;
                }

                List<SpotifyTrackDTO> resultTracks = searchResult == null || searchResult.getTracks() == null
                    ? List.of()
                    : searchResult.getTracks();
                log.info("Assistant query='{}' offset={} results={}", query, page * SEARCH_LIMIT, resultTracks.size());
                for (SpotifyTrackDTO track : resultTracks) {
                    if (track == null || !StringUtils.hasText(track.getId()) || !seen.add(track.getId())) {
                        continue;
                    }
                    fallbackPool.add(track);
                    int score = scoreTrack(track, plan);
                    candidates.add(new ScoredTrack(track, score));
                }
                if (resultTracks.isEmpty()) {
                    break;
                }
            }
            if (fallbackPool.size() >= effectiveTarget || rateLimited) {
                break;
            }
        }

        if (candidates.isEmpty() && fallbackPool.isEmpty() && !rateLimited) {
            log.warn("Assistant primary queries returned no tracks. Trying emergency queries.");
            for (String query : buildEmergencyQueries(plan)) {
                if (!StringUtils.hasText(query)) {
                    continue;
                }
                try {
                    SpotifySearchResultDTO searchResult = spotifySearchService.search(accessToken, query, "track", SEARCH_LIMIT, 0);
                    List<SpotifyTrackDTO> resultTracks = searchResult == null || searchResult.getTracks() == null
                        ? List.of()
                        : searchResult.getTracks();
                    for (SpotifyTrackDTO track : resultTracks) {
                        if (track == null || !StringUtils.hasText(track.getId()) || !seen.add(track.getId())) {
                            continue;
                        }
                        fallbackPool.add(track);
                        candidates.add(new ScoredTrack(track, scoreTrack(track, plan)));
                        if (fallbackPool.size() >= effectiveTarget) {
                            break;
                        }
                    }
                } catch (SpotifyApiException ex) {
                    if (ex.getStatusCode() != null && (ex.getStatusCode() == 400 || ex.getStatusCode() == 403 || ex.getStatusCode() == 429)) {
                        continue;
                    }
                    throw ex;
                }
                if (fallbackPool.size() >= effectiveTarget) {
                    break;
                }
            }
        }

        candidates.sort(Comparator.comparingInt(ScoredTrack::score).reversed());
        log.info("Assistant playlist candidates={} fallbackPool={}", candidates.size(), fallbackPool.size());

        List<AssistantTrackDTO> selected = new ArrayList<>();
        Set<String> selectedIds = new HashSet<>();
        int totalDurationMs = 0;
        int targetDurationMs = targetDurationMinutes == null ? 0 : targetDurationMinutes * 60 * 1000;

        for (ScoredTrack candidate : candidates) {
            SpotifyTrackDTO track = candidate.track();
            if (!selectedIds.add(track.getId())) {
                continue;
            }
            selected.add(toAssistantTrack(track));
            totalDurationMs += track.getDurationMs() == null ? 0 : track.getDurationMs();

            if (targetDurationMs > 0) {
                if (totalDurationMs >= (int) (targetDurationMs * 0.95)) {
                    break;
                }
            } else if (selected.size() >= targetByCount) {
                break;
            }
        }

        if (selected.size() < minTracksToCreate && !fallbackPool.isEmpty()) {
            log.warn("Assistant scoring returned few tracks. Using fallback selection.");
            for (SpotifyTrackDTO track : fallbackPool) {
                if (track == null || !StringUtils.hasText(track.getId()) || !selectedIds.add(track.getId())) {
                    continue;
                }
                selected.add(toAssistantTrack(track));
                totalDurationMs += track.getDurationMs() == null ? 0 : track.getDurationMs();
                if (targetDurationMs > 0) {
                    if (totalDurationMs >= (int) (targetDurationMs * 0.85)) {
                        break;
                    }
                } else if (selected.size() >= targetByCount) {
                    break;
                }
            }
        }

        if (selected.isEmpty() && rateLimited) {
            throw new SpotifyApiException("Spotify ha aplicado rate limit. Espera unos segundos y vuelve a intentarlo.", 429);
        }

        return selected;
    }

    private List<String> buildEmergencyQueries(AssistantPlaylistPlanDTO plan) {
        List<String> queries = new ArrayList<>();
        String genre = plan != null ? plan.getPrimaryGenre() : null;
        String context = plan != null ? plan.getEventContext() : null;
        Integer yearStart = plan != null ? plan.getYearStart() : null;
        Integer yearEnd = plan != null ? plan.getYearEnd() : null;
        boolean is70s = yearStart != null && yearEnd != null && yearStart == 1970 && yearEnd == 1979;
        boolean is80s = yearStart != null && yearEnd != null && yearStart == 1980 && yearEnd == 1989;
        if (StringUtils.hasText(genre) && StringUtils.hasText(context)) {
            queries.add("classic " + genre + " " + context);
        }
        if (StringUtils.hasText(genre)) {
            queries.add("classic " + genre);
            queries.add(genre + " hits");
        }
        if ("wedding".equalsIgnoreCase(context)) {
            queries.add("wedding love songs");
            queries.add("romantic wedding songs");
            queries.add("classic love songs");
        }
        if ("rock".equalsIgnoreCase(genre) && is70s) {
            queries.add("classic rock 70s");
            queries.add("70s rock classics");
            queries.add("rock classics");
        }
        if ("disco".equalsIgnoreCase(genre) && is80s) {
            queries.add("disco 80s classics");
            queries.add("80s dance classics");
            queries.add("funk disco 80s");
        }
        queries.add("calm music");
        queries.add("relaxing music");
        queries.add("study music");
        queries.add("pop hits");
        queries.add("popular songs");
        return new ArrayList<>(new LinkedHashSet<>(queries));
    }

    private int scoreTrack(SpotifyTrackDTO track, AssistantPlaylistPlanDTO plan) {
        String name = safe(track.getName());
        String album = safe(track.getAlbumName());
        String joined = (name + " " + album + " " + String.join(" ", track.getArtists() == null ? List.of() : track.getArtists())).toLowerCase(Locale.ROOT);

        if (Boolean.TRUE.equals(plan.getAvoidExplicit()) && Boolean.TRUE.equals(track.getExplicit())) {
            return -100;
        }

        List<String> negatives = plan.getNegativeConstraints() == null ? List.of() : plan.getNegativeConstraints();
        if ((negatives.contains("no_remix") || negatives.contains("remix")
            || negatives.contains("no_live") || negatives.contains("live")
            || negatives.contains("no_cover") || negatives.contains("cover")
            || negatives.contains("no_karaoke") || negatives.contains("karaoke"))
            && (joined.contains("remix") || joined.contains("live") || joined.contains("cover") || joined.contains("karaoke") || joined.contains("tribute"))) {
            return -100;
        }
        if ((negatives.contains("no_hip_hop") || negatives.contains("hip hop")) && (joined.contains("hip hop") || joined.contains("rap"))) {
            return -80;
        }

        int score = 10;
        Integer popularity = track.getPopularity();
        if (popularity != null) {
            score += Math.max(0, Math.min(30, popularity / 2));
        }

        if (plan.getYearStart() != null && plan.getYearEnd() != null && track.getReleaseYear() != null) {
            if (track.getReleaseYear() >= plan.getYearStart() && track.getReleaseYear() <= plan.getYearEnd()) {
                score += 40;
            } else {
                score -= 40;
            }
        }

        List<String> moodTokens = plan.getMoods() == null ? List.of() : plan.getMoods();
        for (String mood : moodTokens) {
            if (StringUtils.hasText(mood) && joined.contains(mood.toLowerCase(Locale.ROOT))) {
                score += 8;
            }
        }

        if (StringUtils.hasText(plan.getPrimaryGenre())) {
            String genre = plan.getPrimaryGenre().toLowerCase(Locale.ROOT);
            if (joined.contains(genre)) {
                score += 25;
            }
        }

        if ("kids".equalsIgnoreCase(plan.getTargetAudience())) {
            if (joined.contains("kids") || joined.contains("children") || joined.contains("family")) {
                score += 20;
            }
            if (joined.contains("explicit")) {
                score -= 40;
            }
        }

        return score;
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
        List<String> safeUris = uris == null
            ? List.of()
            : uris.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        int total = safeUris.size();
        if (total == 0) {
            throw new ResourceNotFoundException("No se encontraron canciones para crear la playlist.");
        }
        int start = 0;

        while (start < total) {
            int end = Math.min(start + TRACKS_PER_ADD_REQUEST, total);
            List<String> batch = safeUris.subList(start, end);

            spotifyApiClient.postNoContent(
                accessToken,
                "/playlists/" + playlistId + "/items",
                Map.of("uris", batch)
            );

            start = end;
        }
    }

    private SpotifyApiException mapPlaylistCreationError(SpotifyApiException ex) {
        Integer status = ex.getStatusCode();
        if (status != null) {
            if (status == 401) {
                return new SpotifyApiException("Tu sesion con Spotify ha caducado. Vuelve a iniciar sesion.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 403) {
                return new SpotifyApiException("Spotify no ha permitido crear la playlist. Revisa permisos.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 429) {
                return new SpotifyApiException("Spotify ha limitado temporalmente las peticiones. Intentalo mas tarde.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
        }
        return ex;
    }

    private SpotifyApiException mapPlaylistAddTracksError(SpotifyApiException ex) {
        Integer status = ex.getStatusCode();
        if (status != null) {
            if (status == 401) {
                return new SpotifyApiException("Tu sesion con Spotify ha caducado. Vuelve a iniciar sesion.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 403) {
                return new SpotifyApiException("Spotify no ha permitido anadir canciones a la playlist. Revisa permisos.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
            if (status == 429) {
                return new SpotifyApiException("Spotify ha limitado temporalmente las peticiones. Intentalo mas tarde.",
                    status, ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
            }
        }
        return new SpotifyApiException("Se creo la playlist, pero fallo al agregar canciones: " + ex.getMessage(),
            ex.getStatusCode(), ex.getRetryAfterSeconds(), ex.getSpotifyErrorCode(), ex.getSpotifyErrorCategory(), ex);
    }

    private String buildAssistantMessage(String playlistName, int tracksAdded, Integer targetDurationMinutes, List<AssistantTrackDTO> tracks) {
        String safeName = StringUtils.hasText(playlistName) ? playlistName : "tu playlist";
        if (targetDurationMinutes == null) {
            return "He creado tu playlist '" + safeName + "' con " + tracksAdded + " canciones.";
        }
        return "He creado tu playlist '" + safeName + "' con " + tracks.size() + " canciones, orientada a " + targetDurationMinutes + " minutos.";
    }

    private record ScoredTrack(SpotifyTrackDTO track, int score) {}
}
