import { ref } from "vue";
import { TopTracksService } from "../application/TopTracksService";
import { SpotifyTopTracksApiClient } from "../infrastructure/SpotifyTopTracksApiClient";

const defaultTopTracksService = new TopTracksService(new SpotifyTopTracksApiClient());

function resolveErrorMessage(error) {
  return error?.response?.data?.message || "No se pudieron cargar tus top canciones.";
}

export function useTopTracks(topTracksService = defaultTopTracksService) {
  const tracks = ref([]);
  const loading = ref(false);
  const error = ref("");

  async function loadTopTracks(limit = 5, timeRange = "short_term") {
    loading.value = true;
    error.value = "";

    try {
      tracks.value = await topTracksService.getTopTracks(limit, timeRange);
    } catch (requestError) {
      error.value = resolveErrorMessage(requestError);
      tracks.value = [];
    } finally {
      loading.value = false;
    }
  }

  return {
    tracks,
    loading,
    error,
    loadTopTracks,
  };
}
