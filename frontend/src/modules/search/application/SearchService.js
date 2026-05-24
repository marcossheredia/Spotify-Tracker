/**
 * Clase funcional: SearchService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class SearchService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async search(query, types, limit, offset) {
    return this.apiClient.search(query, types, limit, offset);
  }
}
