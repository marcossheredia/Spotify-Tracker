import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyTopArtistsApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyTopArtistsApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getTopArtists(limit = 5, timeRange = "short_term") {
    const { data } = await api.get("/api/spotify/top/artists", {
      params: { limit, timeRange },
    });
    return data;
  }
}
