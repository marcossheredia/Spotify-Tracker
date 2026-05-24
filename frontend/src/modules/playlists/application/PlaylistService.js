import { Playlist } from "../domain/Playlist";
import { PlaylistDetail } from "../domain/PlaylistDetail";

/**
 * Clase funcional: PlaylistService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class PlaylistService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getRecentPlaylists(limit = 5) {
    const playlistsDto = await this.apiClient.getRecentPlaylists(limit);
    return Playlist.fromApiCollection(playlistsDto);
  }

  /** Obtiene datos para esta parte del sistema. */

  async getPlaylistDetail(playlistId, limit = 50) {
    const detailDto = await this.apiClient.getPlaylistDetail(playlistId, limit);
    return PlaylistDetail.fromApi(detailDto);
  }

  /** Obtiene datos para esta parte del sistema. */

  async getAllPlaylists(limit = 20, offset = 0) {
    const pageDto = await this.apiClient.getAllPlaylists(limit, offset);
    return {
      ...pageDto,
      items: Playlist.fromApiCollection(pageDto?.items || []),
    };
  }
}