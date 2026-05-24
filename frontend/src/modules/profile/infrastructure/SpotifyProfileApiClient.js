import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyProfileApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyProfileApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getProfile() {
    const { data } = await api.get("/api/spotify/profile");
    return data;
  }
}
