import api from "@/shared/services/httpClient";

export class SpotifyLibraryApiClient {
  async getSavedTracks(limit = 20, offset = 0) {
    const { data } = await api.get("/api/spotify/library/tracks", {
      params: { limit, offset },
    });
    return data;
  }

  async getSavedAlbums(limit = 20, offset = 0) {
    const { data } = await api.get("/api/spotify/library/albums", {
      params: { limit, offset },
    });
    return data;
  }

  async saveTrack(trackId) {
    const { data } = await api.put(`/api/spotify/library/tracks/${encodeURIComponent(trackId)}`);
    return data;
  }

  async removeTrack(trackId) {
    const { data } = await api.delete(`/api/spotify/library/tracks/${encodeURIComponent(trackId)}`);
    return data;
  }

  async saveAlbum(albumId) {
    const { data } = await api.put(`/api/spotify/library/albums/${encodeURIComponent(albumId)}`);
    return data;
  }

  async removeAlbum(albumId) {
    const { data } = await api.delete(`/api/spotify/library/albums/${encodeURIComponent(albumId)}`);
    return data;
  }
}
