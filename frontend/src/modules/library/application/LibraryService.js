export class LibraryService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getSavedTracks(limit, offset) {
    return this.apiClient.getSavedTracks(limit, offset);
  }

  async getSavedAlbums(limit, offset) {
    return this.apiClient.getSavedAlbums(limit, offset);
  }

  async saveTrack(trackId) {
    return this.apiClient.saveTrack(trackId);
  }

  async removeTrack(trackId) {
    return this.apiClient.removeTrack(trackId);
  }

  async saveAlbum(albumId) {
    return this.apiClient.saveAlbum(albumId);
  }

  async removeAlbum(albumId) {
    return this.apiClient.removeAlbum(albumId);
  }
}
