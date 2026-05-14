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

export class PlaytimeStatsService {
  async syncRecentPlaytime() {
    const { data } = await api.post("/api/spotify/sync/recent-playtime");
    return normalizeStats(data);
  }

  async getPlaytimeStats() {
    const { data } = await api.get("/api/spotify/stats/playtime");
    return normalizeStats(data);
  }

  async getPlaytimeHistory({ from, to, granularity } = {}) {
    const params = {};
    if (from) {
      params.from = from;
    }
    if (to) {
      params.to = to;
    }
    if (granularity) {
      params.granularity = granularity;
    }

    const { data } = await api.get("/api/spotify/stats/playtime/history", { params });
    return normalizeHistory(data);
  }
}
