import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyPlayerApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyPlayerApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getState() {
    const { data } = await api.get("/api/spotify/player/state");
    return data;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getQueue() {
    const { data } = await api.get("/api/spotify/player/queue");
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async play() {
    const { data } = await api.post("/api/spotify/player/play");
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async pause() {
    const { data } = await api.post("/api/spotify/player/pause");
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async next() {
    const { data } = await api.post("/api/spotify/player/next");
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async previous() {
    const { data } = await api.post("/api/spotify/player/previous");
    return data;
  }

  /** Actualiza una configuración o estado. */

  async setRepeat(repeatState) {
    const { data } = await api.post("/api/spotify/player/settings", { repeatState });
    return data;
  }

  /** Actualiza una configuración o estado. */

  async setShuffle(shuffleState) {
    const { data } = await api.post("/api/spotify/player/settings", { shuffleState });
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async transferPlayback(deviceId, play = false) {
    const { data } = await api.post("/api/spotify/player/transfer", { deviceId, play });
    return data;
  }
}
