import { AssistantPlaylistResult } from "../domain/AssistantPlaylistResult";

/**
 * Clase funcional: AssistantService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class AssistantService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Crea un recurso nuevo con los datos recibidos. */

  async createPlaylist(message, options = {}) {
    const payload = {
      message,
      publicPlaylist: Boolean(options.publicPlaylist),
    };

    if (Number.isFinite(Number(options.trackLimit))) {
      payload.trackLimit = Number(options.trackLimit);
    }

    const responseDto = await this.apiClient.createPlaylist(payload);
    return AssistantPlaylistResult.fromApi(responseDto);
  }
}
