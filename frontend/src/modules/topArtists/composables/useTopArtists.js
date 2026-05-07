import { ref } from "vue";
import { TopArtistsService } from "../application/TopArtistsService";
import { SpotifyTopArtistsApiClient } from "../infrastructure/SpotifyTopArtistsApiClient";

const defaultTopArtistsService = new TopArtistsService(new SpotifyTopArtistsApiClient());

function resolveErrorMessage(error) {
  return error?.response?.data?.message || "No se pudieron cargar tus top artistas.";
}

export function useTopArtists(topArtistsService = defaultTopArtistsService) {
  const artists = ref([]);
  const loading = ref(false);
  const error = ref("");

  async function loadTopArtists(limit = 5, timeRange = "short_term") {
    loading.value = true;
    error.value = "";

    try {
      artists.value = await topArtistsService.getTopArtists(limit, timeRange);
    } catch (requestError) {
      error.value = resolveErrorMessage(requestError);
      artists.value = [];
    } finally {
      loading.value = false;
    }
  }

  return {
    artists,
    loading,
    error,
    loadTopArtists,
  };
}
