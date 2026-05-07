import { AssistantPlaylistResult } from "../domain/AssistantPlaylistResult";

export class AssistantService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async createPlaylist(message, options = {}) {
    const payload = {
      message,
      publicPlaylist: Boolean(options.publicPlaylist),
    };

    if (Number.isFinite(Number(options.trackLimit))) {
      payload.trackLimit = Number(options.trackLimit);
    }

    const responseDto = await this.apiClient.createPlaylist(payload);
    return AssistantPlaylistResult.fromApi(responseDto);
  }
}
