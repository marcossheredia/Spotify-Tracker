import { ref } from "vue";
import { SearchService } from "../application/SearchService";
import { SpotifySearchApiClient } from "../infrastructure/SpotifySearchApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultService = new SearchService(new SpotifySearchApiClient());

export function useGlobalSearch(searchService = defaultService) {
  const results = ref({
    query: "",
    tracks: [],
    artists: [],
    albums: [],
    playlists: [],
  });
  const loading = ref(false);
  const error = ref("");

  async function executeSearch(query, types = "track,artist,album,playlist", limit = 10) {
    const safeQuery = String(query || "").trim();
    if (!safeQuery) {
      results.value = {
        query: "",
        tracks: [],
        artists: [],
        albums: [],
        playlists: [],
      };
      return;
    }

    loading.value = true;
    error.value = "";

    try {
      results.value = await searchService.search(safeQuery, types, limit, 0);
    } catch (requestError) {
      results.value = {
        query: safeQuery,
        tracks: [],
        artists: [],
        albums: [],
        playlists: [],
      };
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo completar la busqueda global.");
    } finally {
      loading.value = false;
    }
  }

  return { results, loading, error, executeSearch };
}
