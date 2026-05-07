import api from "@/shared/services/httpClient";

export class SpotifyPlaylistApiClient {
  async getRecentPlaylists(limit = 5) {
    const { data } = await api.get("/api/spotify/playlists/recent", {
      params: { limit },
    });
    return data;
  }

  async getPlaylistDetail(playlistId, limit = 50) {
    const { data } = await api.get(`/api/spotify/playlists/${encodeURIComponent(playlistId)}/detail`, {
      params: { limit },
    });
    return data;
  }

  async getAllPlaylists(limit = 20, offset = 0) {
    const { data } = await api.get("/api/spotify/playlists/all", {
      params: { limit, offset },
    });
    return data;
  }
}