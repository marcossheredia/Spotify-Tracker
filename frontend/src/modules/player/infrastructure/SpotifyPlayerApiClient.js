import api from "@/shared/services/httpClient";

export class SpotifyPlayerApiClient {
  async getState() {
    const { data } = await api.get("/api/spotify/player/state");
    return data;
  }

  async getQueue() {
    const { data } = await api.get("/api/spotify/player/queue");
    return data;
  }

  async play() {
    const { data } = await api.post("/api/spotify/player/play");
    return data;
  }

  async pause() {
    const { data } = await api.post("/api/spotify/player/pause");
    return data;
  }

  async next() {
    const { data } = await api.post("/api/spotify/player/next");
    return data;
  }

  async previous() {
    const { data } = await api.post("/api/spotify/player/previous");
    return data;
  }

  async setRepeat(repeatState) {
    const { data } = await api.post("/api/spotify/player/settings", { repeatState });
    return data;
  }

  async setShuffle(shuffleState) {
    const { data } = await api.post("/api/spotify/player/settings", { shuffleState });
    return data;
  }

  async transferPlayback(deviceId, play = false) {
    const { data } = await api.post("/api/spotify/player/transfer", { deviceId, play });
    return data;
  }
}
