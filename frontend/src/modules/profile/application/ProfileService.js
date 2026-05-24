/**
 * Clase funcional: ProfileService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class ProfileService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getProfile() {
    return this.apiClient.getProfile();
  }
}
