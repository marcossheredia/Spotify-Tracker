import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyAutomationApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyAutomationApiClient {
  /** Crea un recurso nuevo con los datos recibidos. */
  async createTopTracksPlaylist(payload) {
    const { data } = await api.post("/api/spotify/playlists/automations/top-tracks", payload);
    return data;
  }
}
