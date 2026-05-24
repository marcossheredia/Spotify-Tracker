import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyFollowApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyFollowApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getArtists(limit = 20, after = "") {
    const params = { limit };
    if (after) {
      params.after = after;
    }

    const { data } = await api.get("/api/spotify/follow/artists", { params });
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async followArtist(artistId) {
    const { data } = await api.put(`/api/spotify/follow/artists/${encodeURIComponent(artistId)}`);
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async unfollowArtist(artistId) {
    const { data } = await api.delete(`/api/spotify/follow/artists/${encodeURIComponent(artistId)}`);
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async followPlaylist(playlistId) {
    const { data } = await api.put(`/api/spotify/follow/playlists/${encodeURIComponent(playlistId)}`);
    return data;
  }

  /** Ejecuta una parte concreta de la lógica de esta clase. */

  async unfollowPlaylist(playlistId) {
    const { data } = await api.delete(`/api/spotify/follow/playlists/${encodeURIComponent(playlistId)}`);
    return data;
  }
}
