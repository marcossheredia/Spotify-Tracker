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
  const lastSearchKey = ref("");
  const cooldownUntilMs = ref(0);
  const activeRequestId = ref(0);

  async function executeSearch(query, types = "track,artist,album,playlist", limit = 10) {
    const safeQuery = String(query || "").trim();
    const safeTypes = String(types || "").trim();
    if (!safeQuery || safeQuery.length < 3 || !safeTypes) {
      results.value = {
        query: "",
        tracks: [],
        artists: [],
        albums: [],
        playlists: [],
      };
      if (safeQuery && safeQuery.length < 3) {
        error.value = "";
      }
      return;
    }
    const now = Date.now();
    if (cooldownUntilMs.value > now) {
      const seconds = Math.max(1, Math.ceil((cooldownUntilMs.value - now) / 1000));
      error.value = `Spotify aplico rate limit. Reintenta en ${seconds}s.`;
      return;
    }
    const searchKey = `${safeQuery.toLowerCase()}|${safeTypes}|${limit}`;
    if (searchKey === lastSearchKey.value) {
      return;
    }

    loading.value = true;
    error.value = "";
    const requestId = activeRequestId.value + 1;
    activeRequestId.value = requestId;

    try {
      const response = await searchService.search(safeQuery, safeTypes, limit, 0);
      if (requestId !== activeRequestId.value) {
        return;
      }
      results.value = response;
      lastSearchKey.value = searchKey;
    } catch (requestError) {
      if (requestId !== activeRequestId.value) {
        return;
      }
      results.value = {
        query: safeQuery,
        tracks: [],
        artists: [],
        albums: [],
        playlists: [],
      };
      if (requestError?.response?.status === 429) {
        const retryAfter = Number(requestError?.retryAfterSeconds);
        cooldownUntilMs.value = Date.now() + (Number.isFinite(retryAfter) && retryAfter > 0 ? retryAfter * 1000 : 10000);
      }
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo completar la busqueda global.");
    } finally {
      if (requestId === activeRequestId.value) {
        loading.value = false;
      }
    }
  }

  return { results, loading, error, executeSearch };
}
