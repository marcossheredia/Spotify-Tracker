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

export class PlaytimeStatsService {
  async syncRecentPlaytime() {
    const { data } = await api.post("/api/spotify/sync/recent-playtime");
    return normalizeStats(data);
  }

  async getPlaytimeStats() {
    const { data } = await api.get("/api/spotify/stats/playtime");
    return normalizeStats(data);
  }
}
