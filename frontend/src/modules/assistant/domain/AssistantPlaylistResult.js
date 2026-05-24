/**
 * Clase funcional: AssistantPlaylistResult.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class AssistantPlaylistResult {
  constructor({
    playlistId = "",
    playlistName = "",
    externalUrl = null,
    tracksAdded = 0,
    message = "",
    tracks = [],
  } = {}) {
    this.playlistId = playlistId;
    this.playlistName = playlistName;
    this.externalUrl = externalUrl;
    this.tracksAdded = Number.isFinite(Number(tracksAdded)) ? Number(tracksAdded) : 0;
    this.message = message;
    this.tracks = Array.isArray(tracks) ? tracks : [];
  }

  static fromApi(dto = null) {
    if (!dto || typeof dto !== "object") {
      return null;
    }

    const tracks = Array.isArray(dto.tracks)
      ? dto.tracks
          .map((track) => ({
            id: track?.id || "",
            name: track?.name || "",
            artist: track?.artist || "",
            uri: track?.uri || "",
          }))
          .filter((track) => track.id || track.name)
      : [];

    return new AssistantPlaylistResult({
      playlistId: dto.playlistId,
      playlistName: dto.playlistName,
      externalUrl: dto.externalUrl,
      tracksAdded: dto.tracksAdded,
      message: dto.message,
      tracks,
    });
  }
}
