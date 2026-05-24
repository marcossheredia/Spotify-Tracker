function toOptionalNumber(value) {
  if (value === null || value === undefined || value === "") {
    return null;
  }

  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : null;
}

function formatMilliseconds(durationMs) {
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
 * Clase funcional: NowPlayingTrack.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class NowPlayingTrack {
  constructor({
    id = "",
    name = "",
    imageUrl = null,
    artists = [],
    albumName = "",
    externalUrl = null,
    isPlaying = false,
    progressMs = null,
    durationMs = null,
  } = {}) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.artists = Array.isArray(artists) ? artists.filter(Boolean) : [];
    this.albumName = albumName;
    this.externalUrl = externalUrl;
    this.isPlaying = Boolean(isPlaying);
    this.progressMs = toOptionalNumber(progressMs);
    this.durationMs = toOptionalNumber(durationMs);
  }

  get artistsLabel() {
    return this.artists.length ? this.artists.join(", ") : "Artista desconocido";
  }

  get progressPercent() {
    if (!this.durationMs || !this.progressMs || this.durationMs <= 0) {
      return 0;
    }

    const rawPercent = (this.progressMs / this.durationMs) * 100;
    return Math.max(0, Math.min(100, rawPercent));
  }

  get progressLabel() {
    if (!this.durationMs) {
      return "Duracion no disponible";
    }

    const progressText = formatMilliseconds(this.progressMs ?? 0);
    const durationText = formatMilliseconds(this.durationMs);
    return `${progressText} / ${durationText}`;
  }

  static fromApi(dto = null) {
    if (!dto || typeof dto !== "object") {
      return null;
    }

    return new NowPlayingTrack({
      id: dto.id,
      name: dto.name,
      imageUrl: dto.imageUrl,
      artists: dto.artists,
      albumName: dto.albumName,
      externalUrl: dto.externalUrl,
      isPlaying: dto.isPlaying,
      progressMs: dto.progressMs,
      durationMs: dto.durationMs,
    });
  }
}
