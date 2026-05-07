import api from "@/shared/services/httpClient";

export const spotifyService = {
  async getProfile() { const { data } = await api.get("/api/spotify/profile"); return data; },
  async getTopTracks(params = {}) { const { data } = await api.get("/api/spotify/top/tracks", { params }); return data; },
  async getTopArtists(params = {}) { const { data } = await api.get("/api/spotify/top/artists", { params }); return data; },
  async search(params = {}) { const { data } = await api.get("/api/spotify/search", { params }); return data; },
};
