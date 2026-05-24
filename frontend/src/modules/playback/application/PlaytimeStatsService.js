import api from "@/shared/services/httpClient";

function normalizeStats(payload = {}) {
  return {
    addedTracks: Number(payload.addedTracks || 0),
    addedDurationMs: Number(payload.addedDurationMs || 0),
    totalPlaytimeMs: Number(payload.totalPlaytimeMs || 0),
    totalReproducciones: Number(payload.totalReproducciones || 0),
    lastRecentlyPlayedAt: payload.lastRecentlyPlayedAt || null,
    lastSyncAt: payload.lastSyncAt || null,
  };
}

function normalizeHistoryPoint(point = {}) {
  return {
    periodStart: point.periodStart || null,
    totalPlaytimeMs: Number(point.totalPlaytimeMs || 0),
    totalReproducciones: Number(point.totalReproducciones || 0),
  };
}

function normalizeHistory(payload = {}) {
  return {
    from: payload.from || null,
    to: payload.to || null,
    granularity: payload.granularity || "day",
    totalPlaytimeMs: Number(payload.totalPlaytimeMs || 0),
    totalReproducciones: Number(payload.totalReproducciones || 0),
    points: Array.isArray(payload.points) ? payload.points.map(normalizeHistoryPoint) : [],
  };
}

/**
 * Clase funcional: PlaytimeStatsService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class PlaytimeStatsService {
  /** Sincroniza datos para mantenerlos al día. */
  async syncRecentPlaytime() {
    const { data } = await api.post("/api/spotify/sync/recent-playtime");
    return normalizeStats(data);
  }

  /** Obtiene datos para esta parte del sistema. */

  async getPlaytimeStats() {
    const { data } = await api.get("/api/spotify/stats/playtime");
    return normalizeStats(data);
  }

  /** Obtiene datos para esta parte del sistema. */

  async getPlaytimeHistory({ from, to, frequency } = {}) {
    const params = {};
    if (from) {
      params.from = from;
    }
    if (to) {
      params.to = to;
    }
    if (frequency) {
      params.frequency = frequency;
    }

    const { data } = await api.get("/api/playtime/history", { params });
    return normalizeHistory(data);
  }
}
