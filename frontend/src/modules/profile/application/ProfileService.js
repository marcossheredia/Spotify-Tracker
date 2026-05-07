export class ProfileService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getProfile() {
    return this.apiClient.getProfile();
  }
}
