import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyPlaylistApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyPlaylistApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getRecentPlaylists(limit = 5) {
    const { data } = await api.get("/api/spotify/playlists/recent", {
      params: { limit },
    });
    return data;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getPlaylistDetail(playlistId, limit = 50) {
    const { data } = await api.get(`/api/spotify/playlists/${encodeURIComponent(playlistId)}/detail`, {
      params: { limit },
    });
    return data;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getAllPlaylists(limit = 20, offset = 0) {
    const { data } = await api.get("/api/spotify/playlists/all", {
      params: { limit, offset },
    });
    return data;
  }
}