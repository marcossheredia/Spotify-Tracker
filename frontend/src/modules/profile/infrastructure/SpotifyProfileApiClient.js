import api from "@/shared/services/httpClient";

export class SpotifyProfileApiClient {
  async getProfile() {
    const { data } = await api.get("/api/spotify/profile");
    return data;
  }
}
