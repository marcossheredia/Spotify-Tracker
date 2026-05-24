import api from "@/shared/services/httpClient";

/**
 * Clase funcional: SpotifyLibraryApiClient.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class SpotifyLibraryApiClient {
  /** Obtiene datos para esta parte del sistema. */
  async getSavedTracks(limit = 20, offset = 0) {
    const { data } = await api.get("/api/spotify/library/tracks", {
      params: { limit, offset },
    });
    return data;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getSavedAlbums(limit = 20, offset = 0) {
    const { data } = await api.get("/api/spotify/library/albums", {
      params: { limit, offset },
    });
    return data;
  }

  /** Guarda o actualiza datos en el sistema. */

  async saveTrack(trackId) {
    const { data } = await api.put(`/api/spotify/library/tracks/${encodeURIComponent(trackId)}`);
    return data;
  }

  /** Elimina o desvincula datos según el caso. */

  async removeTrack(trackId) {
    const { data } = await api.delete(`/api/spotify/library/tracks/${encodeURIComponent(trackId)}`);
    return data;
  }

  /** Guarda o actualiza datos en el sistema. */

  async saveAlbum(albumId) {
    const { data } = await api.put(`/api/spotify/library/albums/${encodeURIComponent(albumId)}`);
    return data;
  }

  /** Elimina o desvincula datos según el caso. */

  async removeAlbum(albumId) {
    const { data } = await api.delete(`/api/spotify/library/albums/${encodeURIComponent(albumId)}`);
    return data;
  }
}
