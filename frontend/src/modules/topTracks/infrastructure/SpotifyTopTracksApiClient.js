import api from "@/shared/services/httpClient";

export class SpotifyTopTracksApiClient {
  async getTopTracks(limit = 5, timeRange = "short_term") {
    const { data } = await api.get("/api/spotify/top/tracks", {
      params: { limit, timeRange },
    });
    return data;
  }
}
