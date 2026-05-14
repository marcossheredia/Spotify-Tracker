import { ref } from "vue";
import { PlaytimeStatsService } from "../application/PlaytimeStatsService";

const defaultPlaytimeService = new PlaytimeStatsService();

function emptyStats() {
  return {
    addedTracks: 0,
    addedDurationMs: 0,
    totalPlaytimeMs: 0,
    totalReproducciones: 0,
    lastRecentlyPlayedAt: null,
    lastSyncAt: null,
  };
}

function emptyHistory() {
  return {
    from: null,
    to: null,
    granularity: "day",
    totalPlaytimeMs: 0,
    totalReproducciones: 0,
    points: [],
  };
}

function resolveErrorMessage(error) {
  return error?.response?.data?.message || "No se pudieron cargar las estadisticas de reproduccion.";
}

export function usePlaytimeStats(playtimeService = defaultPlaytimeService) {
  const stats = ref(emptyStats());
  const loading = ref(false);
  const error = ref("");
  const isSyncing = ref(false);
  const history = ref(emptyHistory());
  const historyLoading = ref(false);
  const historyError = ref("");

  async function syncRecentPlaytime() {
    if (isSyncing.value) {
      return stats.value;
    }

    isSyncing.value = true;
    loading.value = true;
    error.value = "";

    try {
      stats.value = await playtimeService.syncRecentPlaytime();
      return stats.value;
    } catch (requestError) {
      error.value = resolveErrorMessage(requestError);
      try {
        stats.value = await playtimeService.getPlaytimeStats();
        return stats.value;
      } catch {
        stats.value = emptyStats();
        return stats.value;
      }
    } finally {
      loading.value = false;
      isSyncing.value = false;
    }
  }

  async function loadPlaytimeStats() {
    loading.value = true;
    error.value = "";

    try {
      stats.value = await playtimeService.getPlaytimeStats();
    } catch (requestError) {
      error.value = resolveErrorMessage(requestError);
      stats.value = emptyStats();
    } finally {
      loading.value = false;
    }
  }

  async function loadPlaytimeHistory(filters = {}) {
    historyLoading.value = true;
    historyError.value = "";

    try {
      history.value = await playtimeService.getPlaytimeHistory(filters);
    } catch (requestError) {
      historyError.value = resolveErrorMessage(requestError);
      history.value = emptyHistory();
    } finally {
      historyLoading.value = false;
    }
  }

  return {
    stats,
    loading,
    error,
    isSyncing,
    history,
    historyLoading,
    historyError,
    syncRecentPlaytime,
    loadPlaytimeStats,
    loadPlaytimeHistory,
  };
}
