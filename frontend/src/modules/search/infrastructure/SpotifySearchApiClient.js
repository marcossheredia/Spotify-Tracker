import api from "@/shared/services/httpClient";

export class SpotifySearchApiClient {
  async search(query, types, limit = 10, offset = 0) {
    const { data } = await api.get("/api/spotify/search", {
      params: {
        q: query,
        types,
        limit,
        offset,
      },
    });

    return data;
  }
}
