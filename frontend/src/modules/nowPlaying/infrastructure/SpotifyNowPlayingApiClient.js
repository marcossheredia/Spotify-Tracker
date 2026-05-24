import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyNowPlayingApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyNowPlayingApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getCurrentTrack() {
    const response = await api.get("/api/spotify/player/current");

    if (response.status === 204 || !response.data) {
      return null;
    }

    return response.data;
  }
}
