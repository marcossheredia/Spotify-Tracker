export class SearchService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async search(query, types, limit, offset) {
    return this.apiClient.search(query, types, limit, offset);
  }
}
