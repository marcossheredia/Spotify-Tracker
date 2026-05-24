import { TopArtist } from "../domain/TopArtist";

/**
 * Clase funcional: TopArtistsService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class TopArtistsService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getTopArtists(limit = 5, timeRange = "short_term") {
    const artistsDto = await this.apiClient.getTopArtists(limit, timeRange);
    return TopArtist.fromApiCollection(artistsDto);
  }
}
