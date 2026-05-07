import { NowPlayingTrack } from "../domain/NowPlayingTrack";

export class NowPlayingService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getCurrentTrack() {
    const trackDto = await this.apiClient.getCurrentTrack();
    return NowPlayingTrack.fromApi(trackDto);
  }
}
