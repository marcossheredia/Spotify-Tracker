import api from "@/shared/services/httpClient";

export class SpotifyFollowApiClient {
  async getArtists(limit = 20, after = "") {
    const params = { limit };
    if (after) {
      params.after = after;
    }

    const { data } = await api.get("/api/spotify/follow/artists", { params });
    return data;
  }

  async followArtist(artistId) {
    const { data } = await api.put(`/api/spotify/follow/artists/${encodeURIComponent(artistId)}`);
    return data;
  }

  async unfollowArtist(artistId) {
    const { data } = await api.delete(`/api/spotify/follow/artists/${encodeURIComponent(artistId)}`);
    return data;
  }

  async followPlaylist(playlistId) {
    const { data } = await api.put(`/api/spotify/follow/playlists/${encodeURIComponent(playlistId)}`);
    return data;
  }

  async unfollowPlaylist(playlistId) {
    const { data } = await api.delete(`/api/spotify/follow/playlists/${encodeURIComponent(playlistId)}`);
    return data;
  }
}
