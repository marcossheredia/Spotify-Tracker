/**
 * Clase funcional: PlayerService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class PlayerService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getState() {
    return this.apiClient.getState();
  }

  /** Obtiene datos para esta parte del sistema. */

  async getQueue() {
    return this.apiClient.getQueue();
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async play() {
    return this.apiClient.play();
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async pause() {
    return this.apiClient.pause();
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async next() {
    return this.apiClient.next();
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async previous() {
    return this.apiClient.previous();
  }

  /** Actualiza una configuración o estado. */

  async setRepeat(repeatState) {
    return this.apiClient.setRepeat(repeatState);
  }

  /** Actualiza una configuración o estado. */

  async setShuffle(shuffleState) {
    return this.apiClient.setShuffle(shuffleState);
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async transferPlayback(deviceId, play) {
    return this.apiClient.transferPlayback(deviceId, play);
  }
}
