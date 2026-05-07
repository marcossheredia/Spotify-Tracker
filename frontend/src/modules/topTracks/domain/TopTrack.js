export class TopTrack {
  constructor({
    id = "",
    name = "",
    imageUrl = null,
    artists = [],
    albumName = "",
    externalUrl = null,
    popularity = 0,
  } = {}) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.artists = Array.isArray(artists) ? artists.filter(Boolean) : [];
    this.albumName = albumName;
    this.externalUrl = externalUrl;
    this.popularity = Number.isFinite(Number(popularity)) ? Number(popularity) : 0;
  }

  get artistsLabel() {
    return this.artists.length ? this.artists.join(", ") : "Sin artista";
  }

  static fromApi(dto = {}) {
    return new TopTrack({
      id: dto.id,
      name: dto.name,
      imageUrl: dto.imageUrl,
      artists: dto.artists,
      albumName: dto.albumName,
      externalUrl: dto.externalUrl,
      popularity: dto.popularity,
    });
  }

  static fromApiCollection(items = []) {
    if (!Array.isArray(items)) {
      return [];
    }
    return items.map((item) => TopTrack.fromApi(item));
  }
}
