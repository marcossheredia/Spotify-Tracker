import { ref } from "vue";
import { SearchService } from "../application/SearchService";
import { SpotifySearchApiClient } from "../infrastructure/SpotifySearchApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultService = new SearchService(new SpotifySearchApiClient());
const EMPTY_RESULTS = {
  query: "",
  tracks: [],
  artists: [],
  albums: [],
  playlists: [],
};

function cloneEmptyResults(query = "") {
  return {
    ...EMPTY_RESULTS,
    query,
    tracks: [],
    artists: [],
    albums: [],
    playlists: [],
  };
}

function resolveRetryAfterSeconds(error) {
  const retryAfterHeader = error?.response?.headers?.["retry-after"];
  if (retryAfterHeader && /^\d+$/.test(String(retryAfterHeader))) {
    return Number(retryAfterHeader);
  }

  const details = error?.response?.data?.details || [];
  const retryDetail = Array.isArray(details)
    ? details.find((value) => String(value).startsWith("retry_after_seconds="))
    : null;

  if (retryDetail) {
    const value = Number(String(retryDetail).split("=")[1]);
    if (Number.isFinite(value) && value > 0) {
      return value;
    }
  }

  return 10;
}

export function useGlobalSearch(searchService = defaultService) {
  const results = ref(cloneEmptyResults());
  const loading = ref(false);
  const error = ref("");
  const cooldownUntil = ref(0);
  const lastSearchKey = ref("");

  function clearResults() {
    results.value = cloneEmptyResults();
    error.value = "";
    lastSearchKey.value = "";
  }

  async function executeSearch(query, types = "track,artist,album,playlist", limit = 10) {
    const safeQuery = String(query || "").trim();
    const safeTypes = String(types || "").trim();
    const safeLimit = Math.max(1, Math.min(Number(limit) || 10, 10));

    if (!safeQuery || safeQuery.length < 3 || !safeTypes) {
      results.value = cloneEmptyResults(safeQuery);
      return null;
    }

    const now = Date.now();
    if (cooldownUntil.value > now) {
      const seconds = Math.ceil((cooldownUntil.value - now) / 1000);
      error.value = `Spotify aplico rate limit. Espera ${seconds}s antes de buscar otra vez.`;
      return null;
    }

    const searchKey = `${safeQuery.toLowerCase()}|${safeTypes}|${safeLimit}`;
    if (searchKey === lastSearchKey.value) {
      return results.value;
    }

    loading.value = true;
    error.value = "";
    lastSearchKey.value = searchKey;

    try {
      results.value = await searchService.search(safeQuery, safeTypes, safeLimit, 0);
      return results.value;
    } catch (requestError) {
      results.value = cloneEmptyResults(safeQuery);

      if (requestError?.response?.status === 429) {
        const retryAfterSeconds = resolveRetryAfterSeconds(requestError);
        cooldownUntil.value = Date.now() + retryAfterSeconds * 1000;
      }

      error.value = resolveSpotifyErrorMessage(
        requestError,
        "No se pudo completar la busqueda global."
      );
      return null;
    } finally {
      loading.value = false;
    }
  }

  return {
    results,
    loading,
    error,
    executeSearch,
    clearResults,
  };
}
