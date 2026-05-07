import { ref } from "vue";
import { LibraryService } from "../application/LibraryService";
import { SpotifyLibraryApiClient } from "../infrastructure/SpotifyLibraryApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultService = new LibraryService(new SpotifyLibraryApiClient());

export function useLibrary(libraryService = defaultService) {
  const tracksPage = ref({ items: [], limit: 20, offset: 0, total: 0, hasNext: false });
  const albumsPage = ref({ items: [], limit: 20, offset: 0, total: 0, hasNext: false });
  const loadingTracks = ref(false);
  const loadingAlbums = ref(false);
  const mutating = ref(false);
  const error = ref("");

  async function loadTracks(limit = 20, offset = 0, options = {}) {
    const { append = false } = options;
    loadingTracks.value = true;
    error.value = "";

    try {
      const response = await libraryService.getSavedTracks(limit, offset);
      tracksPage.value = append
        ? {
            ...response,
            items: [...(tracksPage.value.items || []), ...(response.items || [])],
          }
        : response;
    } catch (requestError) {
      tracksPage.value = { items: [], limit, offset, total: 0, hasNext: false };
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudieron cargar tus canciones guardadas.");
    } finally {
      loadingTracks.value = false;
    }
  }

  async function loadAlbums(limit = 20, offset = 0, options = {}) {
    const { append = false } = options;
    loadingAlbums.value = true;
    error.value = "";

    try {
      const response = await libraryService.getSavedAlbums(limit, offset);
      albumsPage.value = append
        ? {
            ...response,
            items: [...(albumsPage.value.items || []), ...(response.items || [])],
          }
        : response;
    } catch (requestError) {
      albumsPage.value = { items: [], limit, offset, total: 0, hasNext: false };
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudieron cargar tus albumes guardados.");
    } finally {
      loadingAlbums.value = false;
    }
  }

  async function toggleTrack(trackId, saved) {
    if (!trackId || mutating.value) {
      return null;
    }

    mutating.value = true;
    error.value = "";

    try {
      if (saved) {
        return await libraryService.removeTrack(trackId);
      }
      return await libraryService.saveTrack(trackId);
    } catch (requestError) {
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo actualizar el estado de la cancion.");
      return null;
    } finally {
      mutating.value = false;
    }
  }

  async function toggleAlbum(albumId, saved) {
    if (!albumId || mutating.value) {
      return null;
    }

    mutating.value = true;
    error.value = "";

    try {
      if (saved) {
        return await libraryService.removeAlbum(albumId);
      }
      return await libraryService.saveAlbum(albumId);
    } catch (requestError) {
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo actualizar el estado del album.");
      return null;
    } finally {
      mutating.value = false;
    }
  }

  return {
    tracksPage,
    albumsPage,
    loadingTracks,
    loadingAlbums,
    mutating,
    error,
    loadTracks,
    loadAlbums,
    toggleTrack,
    toggleAlbum,
  };
}
