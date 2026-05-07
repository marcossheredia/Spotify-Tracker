import api from "@/shared/services/httpClient";

export class AssistantApiClient {
  async createPlaylist(payload) {
    const { data } = await api.post("/api/assistant/playlists/create", payload);
    return data;
  }
}
