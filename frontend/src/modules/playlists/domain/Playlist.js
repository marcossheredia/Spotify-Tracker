/**
 * Clase funcional: Playlist.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class Playlist {
  constructor({
    id = "",
    name = "",
    imageUrl = null,
    tracksTotal = 0,
    ownerName = "",
    externalUrl = null,
    lastPlayedAt = null,
    ownPlaylist = false,
    collaborative = false,
    hasLikedTracks = false,
  } = {}) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.tracksTotal = Number.isFinite(Number(tracksTotal)) ? Number(tracksTotal) : 0;
    this.ownerName = ownerName;
    this.externalUrl = externalUrl;
    this.lastPlayedAt = lastPlayedAt;
    this.ownPlaylist = Boolean(ownPlaylist);
    this.collaborative = Boolean(collaborative);
    this.hasLikedTracks = Boolean(hasLikedTracks);
  }

  static fromApi(dto = {}) {
    return new Playlist({
      id: dto.id,
      name: dto.name,
      imageUrl: dto.imageUrl,
      tracksTotal: dto.tracksTotal,
      ownerName: dto.ownerName,
      externalUrl: dto.externalUrl,
      lastPlayedAt: dto.lastPlayedAt,
      ownPlaylist: dto.ownPlaylist,
      collaborative: dto.collaborative,
      hasLikedTracks: dto.hasLikedTracks,
    });
  }

  static fromApiCollection(items = []) {
    if (!Array.isArray(items)) {
      return [];
    }
    return items.map((item) => Playlist.fromApi(item));
  }
}