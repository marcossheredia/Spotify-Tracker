const FOLLOWERS_NUMBER_FORMATTER = new Intl.NumberFormat("es-ES");

function toOptionalNumber(value) {
  if (value === null || value === undefined || value === "") {
    return null;
  }

  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : null;
}

/**
 * Clase funcional: TopArtist.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class TopArtist {
  constructor({
    id = "",
    name = "",
    imageUrl = null,
    followersTotal = null,
    genres = [],
    externalUrl = null,
    popularity = null,
  } = {}) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.followersTotal = toOptionalNumber(followersTotal);
    this.genres = Array.isArray(genres) ? genres.filter(Boolean) : [];
    this.externalUrl = externalUrl;
    this.popularity = toOptionalNumber(popularity);
  }

  get followersLabel() {
    if (this.followersTotal === null) {
      return "Seguidores no disponibles";
    }

    return `${FOLLOWERS_NUMBER_FORMATTER.format(this.followersTotal)} seguidores`;
  }

  get genresLabel() {
    if (!this.genres.length) {
      return "Sin genero";
    }
    return this.genres.slice(0, 2).join(", ");
  }

  static fromApi(dto = {}) {
    return new TopArtist({
      id: dto.id,
      name: dto.name,
      imageUrl: dto.imageUrl,
      followersTotal: dto.followersTotal,
      genres: dto.genres,
      externalUrl: dto.externalUrl,
      popularity: dto.popularity,
    });
  }

  static fromApiCollection(items = []) {
    if (!Array.isArray(items)) {
      return [];
    }
    return items.map((item) => TopArtist.fromApi(item));
  }
}
