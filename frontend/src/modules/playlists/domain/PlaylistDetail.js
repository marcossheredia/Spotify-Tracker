import { PlaylistTrack } from "./PlaylistTrack";

export class PlaylistDetail {
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
    canLoadTracks = true,
    unavailableReason = "",
    tracks = [],
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
    this.canLoadTracks = canLoadTracks !== false;
    this.unavailableReason = unavailableReason || "";
    this.tracks = PlaylistTrack.fromApiCollection(tracks);
  }

  get likedTracksCount() {
    return this.tracks.reduce((count, track) => count + (track.liked ? 1 : 0), 0);
  }

  static fromApi(dto = {}) {
    return new PlaylistDetail({
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
      canLoadTracks: dto.canLoadTracks,
      unavailableReason: dto.unavailableReason,
      tracks: dto.tracks,
    });
  }
}
