/**
 * Clase funcional: FollowService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class FollowService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getArtists(limit, after) {
    return this.apiClient.getArtists(limit, after);
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async followArtist(artistId) {
    return this.apiClient.followArtist(artistId);
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async unfollowArtist(artistId) {
    return this.apiClient.unfollowArtist(artistId);
  }
}
