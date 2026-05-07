import { Playlist } from "../domain/Playlist";
import { PlaylistDetail } from "../domain/PlaylistDetail";

export class PlaylistService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getRecentPlaylists(limit = 5) {
    const playlistsDto = await this.apiClient.getRecentPlaylists(limit);
    return Playlist.fromApiCollection(playlistsDto);
  }

  async getPlaylistDetail(playlistId, limit = 50) {
    const detailDto = await this.apiClient.getPlaylistDetail(playlistId, limit);
    return PlaylistDetail.fromApi(detailDto);
  }

  async getAllPlaylists(limit = 20, offset = 0) {
    const pageDto = await this.apiClient.getAllPlaylists(limit, offset);
    return {
      ...pageDto,
      items: Playlist.fromApiCollection(pageDto?.items || []),
    };
  }
}