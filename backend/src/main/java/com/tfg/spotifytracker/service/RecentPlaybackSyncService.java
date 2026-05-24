package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.dto.playback.response.PlaytimeHistoryPointDTO;
import com.tfg.spotifytracker.dto.playback.response.PlaytimeHistoryResponseDTO;
import com.tfg.spotifytracker.dto.playback.response.RecentPlaybackSyncResponseDTO;
import com.tfg.spotifytracker.entity.ReproduccionReciente;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.entity.UsuarioEstadisticas;
import com.tfg.spotifytracker.repository.ReproduccionRecienteRepository;
import com.tfg.spotifytracker.repository.UsuarioEstadisticasRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Clase funcional: RecentPlaybackSyncService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: UsuarioEstadisticasRepository, ReproduccionRecienteRepository, SpotifyService, SpotifyTokenService.
 */
public class RecentPlaybackSyncService {

    private static final int RECENT_PLAYED_LIMIT = 50;
    private static final int MAX_INCREMENTAL_PAGINATION_ROUNDS = 20;
    private static final int MAX_INITIAL_BACKFILL_PAGES = 5;
    private static final Set<String> ALLOWED_GRANULARITIES = Set.of("day", "week", "month");

    private final UsuarioEstadisticasRepository usuarioEstadisticasRepository;
    private final ReproduccionRecienteRepository reproduccionRecienteRepository;
    private final SpotifyService spotifyService;
    private final SpotifyTokenService spotifyTokenService;

    @Transactional
    /** Sincroniza datos para mantenerlos al día. */
    public RecentPlaybackSyncResponseDTO syncRecentPlaytime(Usuario usuario) {
        UsuarioEstadisticas estadisticas = getOrCreateEstadisticas(usuario.getId());
        String validAccessToken = spotifyTokenService.getValidAccessToken(usuario);

        Long baselineLastPlayedMs = estadisticas.getLastRecentlyPlayedMs();
        SyncAccumulator syncAccumulator = baselineLastPlayedMs != null
            ? syncIncremental(usuario.getId(), validAccessToken, baselineLastPlayedMs)
            : syncInitialBackfill(usuario.getId(), validAccessToken);

        if (syncAccumulator.addedTracks > 0) {
            estadisticas.setTotalPlaytimeMs(safeLong(estadisticas.getTotalPlaytimeMs()) + syncAccumulator.addedDurationMs);
            estadisticas.setTotalReproducciones(safeLong(estadisticas.getTotalReproducciones()) + syncAccumulator.addedTracks);
        }

        Long currentLastPlayedMs = estadisticas.getLastRecentlyPlayedMs();
        if (syncAccumulator.maxProcessedPlayedAtMs != null
            && (currentLastPlayedMs == null || syncAccumulator.maxProcessedPlayedAtMs > currentLastPlayedMs)) {
            estadisticas.setLastRecentlyPlayedMs(syncAccumulator.maxProcessedPlayedAtMs);
            estadisticas.setLastRecentlyPlayedAt(syncAccumulator.maxProcessedPlayedAt);
        }

        estadisticas.setLastSyncAt(Instant.now());
        UsuarioEstadisticas saved = usuarioEstadisticasRepository.save(estadisticas);

        return toResponse(saved, syncAccumulator.addedTracks, syncAccumulator.addedDurationMs);
    }

    @Transactional
    /** Obtiene datos para esta parte del sistema. */
    public RecentPlaybackSyncResponseDTO getPlaytimeStats(Usuario usuario) {
        UsuarioEstadisticas estadisticas = getOrCreateEstadisticas(usuario.getId());
        return toResponse(estadisticas, 0L, 0L);
    }

    @Transactional(readOnly = true)
    /** Obtiene datos para esta parte del sistema. */
    public PlaytimeHistoryResponseDTO getPlaytimeHistory(Usuario usuario, Instant from, Instant to, String granularity) {
        UUID usuarioId = Objects.requireNonNull(usuario.getId(), "usuarioId no puede ser null");
        Instant resolvedFrom = from;
        Instant resolvedTo = to != null ? to : Instant.now();
        String resolvedGranularity = StringUtils.hasText(granularity) ? granularity : "day";

        if (!ALLOWED_GRANULARITIES.contains(resolvedGranularity)) {
            throw new IllegalArgumentException("Granularidad invalida: " + granularity);
        }

        if (resolvedFrom == null) {
            resolvedFrom = reproduccionRecienteRepository.findFirstPlayedAt(usuarioId);
        }

        if (resolvedFrom == null) {
            return PlaytimeHistoryResponseDTO.builder()
                .from(null)
                .to(resolvedTo)
                .granularity(resolvedGranularity)
                .totalPlaytimeMs(0L)
                .totalReproducciones(0L)
                .points(List.of())
                .build();
        }

        if (resolvedFrom.isAfter(resolvedTo)) {
            Instant swap = resolvedFrom;
            resolvedFrom = resolvedTo;
            resolvedTo = swap;
        }

        List<Object[]> rows = reproduccionRecienteRepository.findPlaytimeHistory(
            usuarioId,
            resolvedFrom,
            resolvedTo,
            resolvedGranularity
        );

        Map<Instant, PlaytimeHistoryPointDTO> pointByPeriod = new LinkedHashMap<>();
        long totalPlaytimeMs = 0L;
        long totalReproducciones = 0L;

        for (Object[] row : rows) {
            Instant periodStart = toInstant(row[0]);
            Long playtimeMs = asLong(row[1]);
            Long reproducciones = asLong(row[2]);
            if (periodStart == null) {
                continue;
            }

            totalPlaytimeMs += safeLong(playtimeMs);
            totalReproducciones += safeLong(reproducciones);

            pointByPeriod.put(periodStart, PlaytimeHistoryPointDTO.builder()
                .periodStart(periodStart)
                .totalPlaytimeMs(safeLong(playtimeMs))
                .totalReproducciones(safeLong(reproducciones))
                .build());
        }

        List<PlaytimeHistoryPointDTO> points = fillMissingPeriods(pointByPeriod, resolvedFrom, resolvedTo, resolvedGranularity);

        return PlaytimeHistoryResponseDTO.builder()
            .from(resolvedFrom)
            .to(resolvedTo)
            .granularity(resolvedGranularity)
            .totalPlaytimeMs(totalPlaytimeMs)
            .totalReproducciones(totalReproducciones)
            .points(points)
            .build();
    }

    @Transactional(readOnly = true)
    /** Obtiene datos para esta parte del sistema. */
    public PlaytimeHistoryResponseDTO getPlaytimeHistoryByDate(Usuario usuario, LocalDate from, LocalDate to, String frequency) {
        String safeFrequency = normalizeFrequency(frequency);
        LocalDate resolvedToDate = to != null ? to : LocalDate.now(ZoneOffset.UTC);
        LocalDate resolvedFromDate = from;
        if (resolvedFromDate == null) {
            Instant firstPlayed = reproduccionRecienteRepository.findFirstPlayedAt(usuario.getId());
            resolvedFromDate = firstPlayed != null
                ? LocalDate.ofInstant(firstPlayed, ZoneOffset.UTC)
                : resolvedToDate.minusDays(30);
        }
        Instant fromInstant = resolvedFromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = resolvedToDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        return getPlaytimeHistory(usuario, fromInstant, toInstant, safeFrequency);
    }

    /** Obtiene datos para esta parte del sistema. */

    private UsuarioEstadisticas getOrCreateEstadisticas(UUID usuarioId) {
        UUID safeUsuarioId = Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");

        return usuarioEstadisticasRepository.findById(safeUsuarioId)
            .orElseGet(() -> {
                UsuarioEstadisticas created = UsuarioEstadisticas.builder()
                    .usuarioId(safeUsuarioId)
                    .totalPlaytimeMs(0L)
                    .totalReproducciones(0L)
                    .build();
                return usuarioEstadisticasRepository.save(Objects.requireNonNull(created, "No se pudo crear estadisticas"));
            });
    }

    /** Sincroniza datos para mantenerlos al día. */

    private SyncAccumulator syncIncremental(UUID usuarioId, String accessToken, Long baselineLastPlayedMs) {
        SyncAccumulator accumulator = new SyncAccumulator();
        Long cursorAfter = baselineLastPlayedMs;

        for (int round = 0; round < MAX_INCREMENTAL_PAGINATION_ROUNDS; round++) {
            Map<String, Object> page = spotifyService.getRecentlyPlayed(accessToken, cursorAfter, RECENT_PLAYED_LIMIT);
            List<Map<String, Object>> items = extractItems(page);
            if (items.isEmpty()) {
                break;
            }

            Long pageMaxProcessedMs = cursorAfter;

            for (Map<String, Object> item : items) {
                ReproduccionReciente reproduccion = toReproduccionReciente(usuarioId, item);
                if (reproduccion == null) {
                    continue;
                }

                long playedAtMs = reproduccion.getPlayedAtMs();
                if (playedAtMs <= baselineLastPlayedMs) {
                    continue;
                }

                pageMaxProcessedMs = maxNullable(pageMaxProcessedMs, playedAtMs);
                accumulator.trackMaxProcessed(reproduccion);

                if (saveIfNew(reproduccion)) {
                    accumulator.addedTracks++;
                    accumulator.addedDurationMs += reproduccion.getDurationMs();
                }
            }

            Long nextAfterCursor = extractCursorAfter(page);
            if (nextAfterCursor == null) {
                nextAfterCursor = pageMaxProcessedMs;
            }

            if (nextAfterCursor == null) {
                break;
            }
            if (cursorAfter != null && nextAfterCursor <= cursorAfter) {
                break;
            }

            cursorAfter = nextAfterCursor;

            if (items.size() < RECENT_PLAYED_LIMIT) {
                break;
            }
        }

        return accumulator;
    }

    /** Sincroniza datos para mantenerlos al día. */

    private SyncAccumulator syncInitialBackfill(UUID usuarioId, String accessToken) {
        SyncAccumulator accumulator = new SyncAccumulator();
        Long cursorBefore = null;

        for (int pageNumber = 0; pageNumber < MAX_INITIAL_BACKFILL_PAGES; pageNumber++) {
            Map<String, Object> page = spotifyService.getRecentlyPlayed(accessToken, null, cursorBefore, RECENT_PLAYED_LIMIT);
            List<Map<String, Object>> items = extractItems(page);
            if (items.isEmpty()) {
                break;
            }

            for (Map<String, Object> item : items) {
                ReproduccionReciente reproduccion = toReproduccionReciente(usuarioId, item);
                if (reproduccion == null) {
                    continue;
                }

                accumulator.trackMaxProcessed(reproduccion);

                if (saveIfNew(reproduccion)) {
                    accumulator.addedTracks++;
                    accumulator.addedDurationMs += reproduccion.getDurationMs();
                }
            }

            Long nextBeforeCursor = extractCursorBefore(page);
            if (nextBeforeCursor == null) {
                break;
            }
            if (cursorBefore != null && nextBeforeCursor >= cursorBefore) {
                break;
            }

            cursorBefore = nextBeforeCursor;

            if (items.size() < RECENT_PLAYED_LIMIT) {
                break;
            }
        }

        return accumulator;
    }

    /** Guarda o actualiza datos en el sistema. */

    private boolean saveIfNew(ReproduccionReciente reproduccion) {
        boolean alreadyExists = reproduccionRecienteRepository.existsByUsuarioIdAndSpotifyTrackIdAndPlayedAtMs(
            reproduccion.getUsuarioId(),
            reproduccion.getSpotifyTrackId(),
            reproduccion.getPlayedAtMs()
        );
        if (alreadyExists) {
            return false;
        }

        try {
            reproduccionRecienteRepository.save(reproduccion);
            return true;
        } catch (DataIntegrityViolationException ex) {
            log.debug(
                "Reproduccion duplicada ignorada para usuario={} played_at_ms={}",
                reproduccion.getUsuarioId(),
                reproduccion.getPlayedAtMs()
            );
            return false;
        }
    }

    private RecentPlaybackSyncResponseDTO toResponse(UsuarioEstadisticas estadisticas,
                                                     long addedTracks,
                                                     long addedDurationMs) {
        return RecentPlaybackSyncResponseDTO.builder()
            .addedTracks(addedTracks)
            .addedDurationMs(addedDurationMs)
            .totalPlaytimeMs(safeLong(estadisticas.getTotalPlaytimeMs()))
            .totalReproducciones(safeLong(estadisticas.getTotalReproducciones()))
            .lastRecentlyPlayedAt(estadisticas.getLastRecentlyPlayedAt())
            .lastSyncAt(estadisticas.getLastSyncAt())
            .build();
    }

    /** Transforma datos de un formato a otro. */

    private ReproduccionReciente toReproduccionReciente(UUID usuarioId, Map<String, Object> item) {
        Map<String, Object> track = asMap(item.get("track"));
        String spotifyTrackId = asString(track.get("id"));
        if (!StringUtils.hasText(spotifyTrackId)) {
            return null;
        }

        Instant playedAt = parseInstant(item.get("played_at"));
        if (playedAt == null) {
            return null;
        }

        Map<String, Object> album = asMap(track.get("album"));
        Map<String, Object> trackUrls = asMap(track.get("external_urls"));
        Map<String, Object> context = asMap(item.get("context"));

        return ReproduccionReciente.builder()
            .usuarioId(usuarioId)
            .spotifyTrackId(spotifyTrackId)
            .trackName(asString(track.get("name")))
            .artistNames(String.join(", ", extractNamedValues(track.get("artists"), "name")))
            .albumName(asString(album.get("name")))
            .imageUrl(extractFirstImageUrl(album))
            .spotifyUrl(asString(trackUrls.get("spotify")))
            .playedAt(playedAt)
            .playedAtMs(playedAt.toEpochMilli())
            .durationMs(Math.max(0, asInteger(track.get("duration_ms"))))
            .contextType(asString(context.get("type")))
            .contextUri(asString(context.get("uri")))
            .build();
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private Long extractCursorAfter(Map<String, Object> response) {
        Map<String, Object> cursors = asMap(response.get("cursors"));
        return asLong(cursors.get("after"));
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private Long extractCursorBefore(Map<String, Object> response) {
        Map<String, Object> cursors = asMap(response.get("cursors"));
        return asLong(cursors.get("before"));
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private List<Map<String, Object>> extractItems(Map<String, Object> response) {
        Object itemsObj = response.get("items");
        if (!(itemsObj instanceof List<?> items)) {
            return List.of();
        }

        List<Map<String, Object>> parsedItems = new ArrayList<>();
        for (Object item : items) {
            parsedItems.add(asMap(item));
        }
        return parsedItems;
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private List<String> extractNamedValues(Object value, String fieldName) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> names = new ArrayList<>();
        for (Object element : list) {
            Map<String, Object> elementMap = asMap(element);
            String name = asString(elementMap.get(fieldName));
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
        }
        return names;
    }

    /** Extrae un valor concreto desde una estructura más grande. */

    private String extractFirstImageUrl(Map<String, Object> album) {
        Object imagesObj = album.get("images");
        if (!(imagesObj instanceof List<?> images) || images.isEmpty()) {
            return null;
        }

        Map<String, Object> firstImage = asMap(images.get(0));
        return asString(firstImage.get("url"));
    }

    /** Convierte texto o datos en un formato utilizable. */

    private Instant parseInstant(Object value) {
        String text = asString(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }

        try {
            return Instant.parse(text);
        } catch (Exception ex) {
            log.debug("No se pudo parsear played_at={}", text);
            return null;
        }
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }

        Map<String, Object> map = new HashMap<>();
        rawMap.forEach((key, mapValue) -> {
            if (key != null) {
                map.put(String.valueOf(key), mapValue);
            }
        });
        return map;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String text && text.matches("-?\\d+")) {
            return Integer.parseInt(text);
        }

        return 0;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text && text.matches("-?\\d+")) {
            return Long.parseLong(text);
        }

        return null;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    /** Transforma datos de un formato a otro. */

    private Instant toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }

        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }

        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toInstant();
        }

        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Instant.parse(text);
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Long maxNullable(Long base, Long candidate) {
        if (candidate == null) {
            return base;
        }
        if (base == null) {
            return candidate;
        }
        return Math.max(base, candidate);
    }

    private List<PlaytimeHistoryPointDTO> fillMissingPeriods(Map<Instant, PlaytimeHistoryPointDTO> source,
                                                             Instant from,
                                                             Instant to,
                                                             String granularity) {
        List<PlaytimeHistoryPointDTO> points = new ArrayList<>();
        Instant cursor = truncateInstant(from, granularity);
        Instant end = truncateInstant(to, granularity);
        while (cursor != null && !cursor.isAfter(end)) {
            PlaytimeHistoryPointDTO existing = source.get(cursor);
            if (existing != null) {
                points.add(existing);
            } else {
                points.add(PlaytimeHistoryPointDTO.builder()
                    .periodStart(cursor)
                    .totalPlaytimeMs(0L)
                    .totalReproducciones(0L)
                    .build());
            }
            cursor = advance(cursor, granularity);
        }
        return points;
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Instant truncateInstant(Instant instant, String granularity) {
        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        return switch (granularity) {
            case "week" -> zdt.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            case "month" -> zdt.withDayOfMonth(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            default -> zdt.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        };
    }

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    private Instant advance(Instant instant, String granularity) {
        return switch (granularity) {
            case "week" -> instant.plusSeconds(7L * 24L * 3600L);
            case "month" -> instant.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
            default -> instant.plusSeconds(24L * 3600L);
        };
    }

    /** Normaliza el valor de entrada para evitar errores. */

    private String normalizeFrequency(String frequency) {
        if (!StringUtils.hasText(frequency)) {
            return "day";
        }
        String value = frequency.trim().toLowerCase();
        if ("day".equals(value) || "week".equals(value) || "month".equals(value)) {
            return value;
        }
        return "day";
    }

    private static final class SyncAccumulator {
        private long addedTracks;
        private long addedDurationMs;
        private Long maxProcessedPlayedAtMs;
        private Instant maxProcessedPlayedAt;

        /** Ejecuta una parte concreta de la lógica de esta clase. */

        private void trackMaxProcessed(ReproduccionReciente reproduccion) {
            Long playedAtMs = reproduccion.getPlayedAtMs();
            if (playedAtMs == null) {
                return;
            }

            if (maxProcessedPlayedAtMs == null || playedAtMs > maxProcessedPlayedAtMs) {
                maxProcessedPlayedAtMs = playedAtMs;
                maxProcessedPlayedAt = reproduccion.getPlayedAt();
            }
        }
    }
}
