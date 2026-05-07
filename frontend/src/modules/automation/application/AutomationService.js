export class AutomationService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async createTopTracksPlaylist(payload) {
    return this.apiClient.createTopTracksPlaylist(payload);
  }
}
