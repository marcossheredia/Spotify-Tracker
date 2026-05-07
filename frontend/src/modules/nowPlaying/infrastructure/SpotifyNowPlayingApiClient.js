import api from "@/shared/services/httpClient";

export class SpotifyNowPlayingApiClient {
  async getCurrentTrack() {
    const response = await api.get("/api/spotify/player/current");

    if (response.status === 204 || !response.data) {
      return null;
    }

    return response.data;
  }
}
