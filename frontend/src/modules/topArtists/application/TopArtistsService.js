import { TopArtist } from "../domain/TopArtist";

export class TopArtistsService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getTopArtists(limit = 5, timeRange = "short_term") {
    const artistsDto = await this.apiClient.getTopArtists(limit, timeRange);
    return TopArtist.fromApiCollection(artistsDto);
  }
}
