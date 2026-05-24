import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifySearchApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifySearchApiClient {
  /** Ejecuta una parte concreta de la lógica de esta clase. */
  async search(query, types, limit = 10, offset = 0) {
    const { data } = await api.get("/api/spotify/search", {
      params: {
        q: query,
        types,
        limit,
        offset,
      },
    });

    return data;
  }
}
