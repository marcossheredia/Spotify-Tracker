import api from "@/shared/services/httpClient";

/**
 * Clase funcional: AssistantApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class AssistantApiClient {
  /** Crea un recurso nuevo con los datos recibidos. */
  async createPlaylist(payload) {
    const { data } = await api.post("/api/assistant/playlists/create", payload);
    return data;
  }
}
