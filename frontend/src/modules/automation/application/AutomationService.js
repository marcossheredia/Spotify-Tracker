/**
 * Clase funcional: AutomationService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class AutomationService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Crea un recurso nuevo con los datos recibidos. */

  async createTopTracksPlaylist(payload) {
    return this.apiClient.createTopTracksPlaylist(payload);
  }
}
