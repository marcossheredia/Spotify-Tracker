export class FollowService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getArtists(limit, after) {
    return this.apiClient.getArtists(limit, after);
  }

  async followArtist(artistId) {
    return this.apiClient.followArtist(artistId);
  }

  async unfollowArtist(artistId) {
    return this.apiClient.unfollowArtist(artistId);
  }
}
