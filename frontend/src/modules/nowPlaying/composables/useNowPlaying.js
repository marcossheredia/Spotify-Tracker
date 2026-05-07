import { ref } from "vue";
import { NowPlayingService } from "../application/NowPlayingService";
import { SpotifyNowPlayingApiClient } from "../infrastructure/SpotifyNowPlayingApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultNowPlayingService = new NowPlayingService(new SpotifyNowPlayingApiClient());

function resolveErrorMessage(error) {
  const backendMessage = error?.response?.data?.message || "";

  if (backendMessage.includes("Permissions missing")) {
    return "Necesitas cerrar sesion y volver a entrar para conceder permisos de reproduccion actual en Spotify.";
  }

  if (error?.response?.status === 403) {
    return "Spotify no ha concedido permisos para ver la reproduccion actual. Cierra sesion y vuelve a entrar.";
  }

  return resolveSpotifyErrorMessage(error, "No se pudo cargar la cancion en reproduccion.");
}

export function useNowPlaying(nowPlayingService = defaultNowPlayingService) {
  const currentTrack = ref(null);
  const loading = ref(false);
  const error = ref("");
  const isRefreshing = ref(false);

  async function loadCurrentTrack(options = {}) {
    const { silent = false } = options;

    if (isRefreshing.value) {
      return currentTrack.value;
    }

    isRefreshing.value = true;

    if (!silent) {
      loading.value = true;
      error.value = "";
    }

    try {
      currentTrack.value = await nowPlayingService.getCurrentTrack();
      error.value = "";
      return currentTrack.value;
    } catch (requestError) {
      if (!silent || !currentTrack.value) {
        error.value = resolveErrorMessage(requestError);
        currentTrack.value = null;
      }
      return currentTrack.value;
    } finally {
      if (!silent) {
        loading.value = false;
      }
      isRefreshing.value = false;
    }
  }

  return {
    currentTrack,
    loading,
    error,
    loadCurrentTrack,
  };
}
