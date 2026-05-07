import { TopTrack } from "../domain/TopTrack";

export class TopTracksService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getTopTracks(limit = 5, timeRange = "short_term") {
    const tracksDto = await this.apiClient.getTopTracks(limit, timeRange);
    return TopTrack.fromApiCollection(tracksDto);
  }
}
