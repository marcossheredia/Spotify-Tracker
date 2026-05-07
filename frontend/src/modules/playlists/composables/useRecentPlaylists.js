import { ref } from "vue";
import { PlaylistService } from "../application/PlaylistService";
import { SpotifyPlaylistApiClient } from "../infrastructure/SpotifyPlaylistApiClient";

const defaultPlaylistService = new PlaylistService(new SpotifyPlaylistApiClient());

function resolveErrorMessage(error) {
  return error?.response?.data?.message || "No se pudieron cargar tus playlists.";
}

export function useRecentPlaylists(playlistService = defaultPlaylistService) {
  const playlists = ref([]);
  const allPlaylistsPage = ref({ items: [], limit: 20, offset: 0, total: 0, hasNext: false });
  const loading = ref(false);
  const loadingAll = ref(false);
  const error = ref("");
  const playlistDetailById = ref({});
  const playlistDetailLoadingById = ref({});
  const playlistDetailErrorById = ref({});

  async function loadRecentPlaylists(limit = 5) {
    loading.value = true;
    error.value = "";

    try {
      playlists.value = await playlistService.getRecentPlaylists(limit);
    } catch (requestError) {
      error.value = resolveErrorMessage(requestError);
      playlists.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function loadAllPlaylists(limit = 20, offset = 0) {
    loadingAll.value = true;
    error.value = "";

    try {
      allPlaylistsPage.value = await playlistService.getAllPlaylists(limit, offset);
    } catch (requestError) {
      allPlaylistsPage.value = { items: [], limit, offset, total: 0, hasNext: false };
      error.value = resolveErrorMessage(requestError);
    } finally {
      loadingAll.value = false;
    }
  }

  async function loadPlaylistDetail(playlistId, options = {}) {
    const { limit = 50, force = false } = options;
    const safePlaylistId = String(playlistId || "").trim();
    if (!safePlaylistId) {
      return null;
    }

    if (!force && playlistDetailById.value[safePlaylistId]) {
      return playlistDetailById.value[safePlaylistId];
    }

    playlistDetailLoadingById.value = {
      ...playlistDetailLoadingById.value,
      [safePlaylistId]: true,
    };
    playlistDetailErrorById.value = {
      ...playlistDetailErrorById.value,
      [safePlaylistId]: "",
    };

    try {
      const detail = await playlistService.getPlaylistDetail(safePlaylistId, limit);
      playlistDetailById.value = {
        ...playlistDetailById.value,
        [safePlaylistId]: detail,
      };
      return detail;
    } catch (requestError) {
      playlistDetailErrorById.value = {
        ...playlistDetailErrorById.value,
        [safePlaylistId]: resolveErrorMessage(requestError),
      };
      return null;
    } finally {
      playlistDetailLoadingById.value = {
        ...playlistDetailLoadingById.value,
        [safePlaylistId]: false,
      };
    }
  }

  function getPlaylistDetail(playlistId) {
    const safePlaylistId = String(playlistId || "").trim();
    return safePlaylistId ? playlistDetailById.value[safePlaylistId] || null : null;
  }

  return {
    playlists,
    allPlaylistsPage,
    loading,
    loadingAll,
    error,
    loadRecentPlaylists,
    loadAllPlaylists,
    playlistDetailById,
    playlistDetailLoadingById,
    playlistDetailErrorById,
    loadPlaylistDetail,
    getPlaylistDetail,
  };
}