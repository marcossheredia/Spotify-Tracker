/**
 * Clase funcional: LibraryService.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: apiClient (cliente de peticiones al backend).
 */
export class LibraryService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  /** Obtiene datos para esta parte del sistema. */

  async getSavedTracks(limit, offset) {
    return this.apiClient.getSavedTracks(limit, offset);
  }

  /** Obtiene datos para esta parte del sistema. */

  async getSavedAlbums(limit, offset) {
    return this.apiClient.getSavedAlbums(limit, offset);
  }

  /** Guarda o actualiza datos en el sistema. */

  async saveTrack(trackId) {
    return this.apiClient.saveTrack(trackId);
  }

  /** Elimina o desvincula datos según el caso. */

  async removeTrack(trackId) {
    return this.apiClient.removeTrack(trackId);
  }

  /** Guarda o actualiza datos en el sistema. */

  async saveAlbum(albumId) {
    return this.apiClient.saveAlbum(albumId);
  }

  /** Elimina o desvincula datos según el caso. */

  async removeAlbum(albumId) {
    return this.apiClient.removeAlbum(albumId);
  }
}
