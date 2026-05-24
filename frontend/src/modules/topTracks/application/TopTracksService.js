import { TopTrack } from "../domain/TopTrack";

/**
 * Clase funcional: TopTracksService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class TopTracksService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getTopTracks(limit = 5, timeRange = "short_term") {
    const tracksDto = await this.apiClient.getTopTracks(limit, timeRange);
    return TopTrack.fromApiCollection(tracksDto);
  }
}
