import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyTopTracksApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyTopTracksApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getTopTracks(limit = 5, timeRange = "short_term") {
    const { data } = await api.get("/api/spotify/top/tracks", {
      params: { limit, timeRange },
    });
    return data;
  }
}
