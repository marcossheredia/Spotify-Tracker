function toOptionalNumber(value) {
  if (value === null || value === undefined || value === "") {
    return null;
  }

  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : null;
}

function formatDuration(durationMs) {
  const safeDuration = toOptionalNumber(durationMs);
  if (safeDuration === null || safeDuration < 0) {
    return "--:--";
  }

  const totalSeconds = Math.floor(safeDuration / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

/**
 * Clase funcional: PlaylistTrack.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class PlaylistTrack {
  constructor({
    id = "",
    name = "",
    artists = [],
    albumName = "",
    durationMs = null,
    externalUrl = null,
    liked = false,
  } = {}) {
    this.id = id;
    this.name = name;
    this.artists = Array.isArray(artists) ? artists.filter(Boolean) : [];
    this.albumName = albumName;
    this.durationMs = toOptionalNumber(durationMs);
    this.externalUrl = externalUrl;
    this.liked = Boolean(liked);
  }

  get artistsLabel() {
    return this.artists.length ? this.artists.join(", ") : "Artista desconocido";
  }

  get durationLabel() {
    return formatDuration(this.durationMs);
  }

  static fromApi(dto = {}) {
    return new PlaylistTrack({
      id: dto.id,
      name: dto.name,
      artists: dto.artists,
      albumName: dto.albumName,
      durationMs: dto.durationMs,
      externalUrl: dto.externalUrl,
      liked: dto.liked,
    });
  }

  static fromApiCollection(items = []) {
    if (!Array.isArray(items)) {
      return [];
    }

    return items.map((item) => PlaylistTrack.fromApi(item));
  }
}
