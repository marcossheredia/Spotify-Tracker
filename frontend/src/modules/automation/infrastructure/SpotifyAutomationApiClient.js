import api from "@/shared/services/httpClient";

export class SpotifyAutomationApiClient {
  async createTopTracksPlaylist(payload) {
    const { data } = await api.post("/api/spotify/playlists/automations/top-tracks", payload);
    return data;
  }
}
