import { NowPlayingTrack } from "../domain/NowPlayingTrack";

/**
 * Clase funcional: NowPlayingService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class NowPlayingService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getCurrentTrack() {
    const trackDto = await this.apiClient.getCurrentTrack();
    return NowPlayingTrack.fromApi(trackDto);
  }
}
