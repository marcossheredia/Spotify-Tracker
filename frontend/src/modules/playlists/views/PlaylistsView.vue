<template>
  <div class="playlists-view">
    <header class="view-header">
      <h1>Tus Playlist</h1>
      <p>Explora tus playlists con filtros, orden por recientes y detalle de canciones.</p>
    </header>

    <PlaylistToolbar
      :search-query="searchQuery"
      :sort-by="sortBy"
      :active-filter="activeFilter"
      :view-mode="viewMode"
      @update:searchQuery="searchQuery = $event"
      @update:sortBy="sortBy = $event"
      @update:activeFilter="activeFilter = $event"
      @update:viewMode="viewMode = $event"
    />

    <div class="source-toggle" role="tablist" aria-label="Origen de playlists">
      <button
        type="button"
        class="source-chip"
        :class="{ active: playlistSource === 'recent' }"
        @click="playlistSource = 'recent'"
      >
        Recientes
      </button>
      <button
        type="button"
        class="source-chip"
        :class="{ active: playlistSource === 'inventory' }"
        @click="playlistSource = 'inventory'"
      >
        Inventario completo
      </button>
    </div>

    <p v-if="playlistsError" class="error-message">{{ playlistsError }}</p>

    <PlaylistSkeleton
      v-else-if="activeLoading"
      :mode="viewMode"
      :count="8"
    />

    <template v-else>
      <p v-if="!activePlaylists.length" class="empty-message">No hay playlists para esta fuente.</p>
      <p v-else-if="!processedPlaylists.length" class="empty-message">
        No hay playlists para el filtro actual. Prueba otra busqueda o chip.
      </p>

      <RecentPlaylistsCard
        v-else-if="viewMode === 'list'"
        :playlists="processedPlaylists"
        :active-playlist-id="selectedPlaylistId"
        :preview-tracks-by-playlist-id="previewTracksByPlaylistId"
        @select="openPlaylistDetail"
        @preview="previewPlaylist"
      />

      <PlaylistGrid
        v-else
        :playlists="processedPlaylists"
        :active-playlist-id="selectedPlaylistId"
        :preview-tracks-by-playlist-id="previewTracksByPlaylistId"
        @select="openPlaylistDetail"
        @preview="previewPlaylist"
      />
    </template>

    <PlaylistDetailDrawer
      :open="Boolean(selectedPlaylistId)"
      :playlist="selectedPlaylistForDrawer"
      :detail="selectedPlaylistDetail"
      :loading="selectedPlaylistLoading"
      :request-error="selectedPlaylistError"
      :track-filter="detailTrackFilter"
      @close="closePlaylistDetail"
      @update:trackFilter="detailTrackFilter = $event"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import PlaylistToolbar from "@/modules/playlists/components/PlaylistToolbar.vue";
import RecentPlaylistsCard from "@/modules/playlists/components/RecentPlaylistsCard.vue";
import PlaylistGrid from "@/modules/playlists/components/PlaylistGrid.vue";
import PlaylistSkeleton from "@/modules/playlists/components/PlaylistSkeleton.vue";
import PlaylistDetailDrawer from "@/modules/playlists/components/PlaylistDetailDrawer.vue";
import { useRecentPlaylists } from "@/modules/playlists/composables/useRecentPlaylists";

const PLAYLISTS_VIEW_MODE_STORAGE_KEY = "spotify_tracker_playlists_view_mode";

const searchQuery = ref("");
const sortBy = ref("recent");
const activeFilter = ref("all");
const playlistSource = ref("recent");
const selectedPlaylistId = ref("");
const detailTrackFilter = ref("all");
const viewMode = ref(resolveInitialViewMode());

const {
  playlists: recentPlaylists,
  allPlaylistsPage,
  loading: playlistsLoading,
  loadingAll,
  error: playlistsError,
  loadRecentPlaylists,
  loadAllPlaylists,
  playlistDetailById,
  playlistDetailLoadingById,
  playlistDetailErrorById,
  loadPlaylistDetail,
  getPlaylistDetail,
} = useRecentPlaylists();

const activePlaylists = computed(() =>
  playlistSource.value === "recent"
    ? recentPlaylists.value
    : allPlaylistsPage.value.items || []
);

const activeLoading = computed(() =>
  playlistSource.value === "recent" ? playlistsLoading.value : loadingAll.value
);

const normalizedSearchQuery = computed(() => searchQuery.value.trim().toLowerCase());

const selectedPlaylist = computed(() =>
  activePlaylists.value.find((playlist) => playlist.id === selectedPlaylistId.value) || null
);

const selectedPlaylistDetail = computed(() => getPlaylistDetail(selectedPlaylistId.value));

const selectedPlaylistLoading = computed(() =>
  Boolean(playlistDetailLoadingById.value[selectedPlaylistId.value])
);

const selectedPlaylistError = computed(() =>
  playlistDetailErrorById.value[selectedPlaylistId.value] || ""
);

const selectedPlaylistForDrawer = computed(() => {
  if (!selectedPlaylist.value) {
    return null;
  }

  if (!selectedPlaylistDetail.value) {
    return selectedPlaylist.value;
  }

  return {
    ...selectedPlaylistDetail.value,
    ...selectedPlaylist.value,
    lastPlayedAt: selectedPlaylist.value.lastPlayedAt || selectedPlaylistDetail.value.lastPlayedAt,
  };
});

const previewTracksByPlaylistId = computed(() => {
  const previewById = {};
  const details = playlistDetailById.value || {};

  for (const [playlistId, detail] of Object.entries(details)) {
    if (!Array.isArray(detail?.tracks) || !detail.tracks.length) {
      continue;
    }

    previewById[playlistId] = detail.tracks
      .slice(0, 5)
      .map((track) => track?.name)
      .filter(Boolean);
  }

  return previewById;
});

const processedPlaylists = computed(() => {
  const query = normalizedSearchQuery.value;

  const filtered = activePlaylists.value.filter((playlist) => {
    const playlistName = String(playlist.name || "").toLowerCase();
    const ownerName = String(playlist.ownerName || "").toLowerCase();
    const matchesQuery = !query || playlistName.includes(query) || ownerName.includes(query);

    if (!matchesQuery) {
      return false;
    }

    switch (activeFilter.value) {
      case "recent":
        return Boolean(playlist.lastPlayedAt);
      case "liked":
        return Boolean(playlist.hasLikedTracks);
      case "owned":
        return Boolean(playlist.ownPlaylist);
      case "collab":
        return Boolean(playlist.collaborative);
      default:
        return true;
    }
  });

  return filtered.slice().sort((left, right) => sortPlaylists(left, right, sortBy.value));
});

watch(viewMode, (nextMode) => {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.setItem(PLAYLISTS_VIEW_MODE_STORAGE_KEY, nextMode);
});

onMounted(async () => {
  await loadRecentPlaylists(500);
  await loadAllPlaylists(50, 0);
  warmupPlaylistsPreview();
});

function resolveInitialViewMode() {
  if (typeof window === "undefined") {
    return "list";
  }

  const storedMode = window.localStorage.getItem(PLAYLISTS_VIEW_MODE_STORAGE_KEY);
  return storedMode === "grid" ? "grid" : "list";
}

function sortPlaylists(left, right, criteria) {
  if (criteria === "name") {
    return String(left.name || "").localeCompare(String(right.name || ""), "es");
  }

  if (criteria === "tracks") {
    const tracksDiff = Number(right.tracksTotal || 0) - Number(left.tracksTotal || 0);
    if (tracksDiff !== 0) {
      return tracksDiff;
    }
    return String(left.name || "").localeCompare(String(right.name || ""), "es");
  }

  const leftDate = left.lastPlayedAt ? Date.parse(left.lastPlayedAt) : NaN;
  const rightDate = right.lastPlayedAt ? Date.parse(right.lastPlayedAt) : NaN;
  const leftHasDate = Number.isFinite(leftDate);
  const rightHasDate = Number.isFinite(rightDate);

  if (leftHasDate && rightHasDate) {
    if (rightDate !== leftDate) {
      return rightDate - leftDate;
    }
  } else if (leftHasDate !== rightHasDate) {
    return leftHasDate ? -1 : 1;
  }

  return String(left.name || "").localeCompare(String(right.name || ""), "es");
}

function previewPlaylist(playlist) {
  if (!playlist?.id) {
    return;
  }

  loadPlaylistDetail(playlist.id, { limit: 5 });
}

function openPlaylistDetail(playlist) {
  if (!playlist?.id) {
    return;
  }

  selectedPlaylistId.value = playlist.id;
  detailTrackFilter.value = "all";
  loadPlaylistDetail(playlist.id, { limit: 100, force: true });
}

function closePlaylistDetail() {
  selectedPlaylistId.value = "";
}

function warmupPlaylistsPreview() {
  const previewTargets = activePlaylists.value.slice(0, 6);
  for (const playlist of previewTargets) {
    if (playlist?.id) {
      loadPlaylistDetail(playlist.id, { limit: 5 });
    }
  }
}
</script>

<style scoped>
.playlists-view {
  min-height: calc(100vh - 64px);
  background: #121212;
  color: #fff;
  padding: 2rem;
}

.view-header {
  margin-bottom: 1rem;
}

.view-header h1 {
  font-size: 1.75rem;
  margin-bottom: 0.4rem;
}

.view-header p {
  color: #b3b3b3;
  max-width: 700px;
}

.source-toggle {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.9rem;
}

.source-chip {
  border: 1px solid #3f3f3f;
  background: #191919;
  color: #fff;
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
  cursor: pointer;
}

.source-chip.active {
  border-color: #1db954;
}

.error-message {
  border: 1px solid #684040;
  background: #2b1b1b;
  color: #ffb4b4;
  border-radius: 10px;
  padding: 0.75rem 0.9rem;
}

.empty-message {
  color: #adadad;
  background: #191919;
  border: 1px dashed #3c3c3c;
  border-radius: 10px;
  padding: 0.8rem;
}

@media (max-width: 768px) {
  .playlists-view {
    padding: 1rem;
  }

  .view-header h1 {
    font-size: 1.42rem;
  }
}
</style>
