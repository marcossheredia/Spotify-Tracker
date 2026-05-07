import api from "@/shared/services/httpClient";

export class SpotifyTopArtistsApiClient {
  async getTopArtists(limit = 5, timeRange = "short_term") {
    const { data } = await api.get("/api/spotify/top/artists", {
      params: { limit, timeRange },
    });
    return data;
  }
}
