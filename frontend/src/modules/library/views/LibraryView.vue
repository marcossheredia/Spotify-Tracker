<template>
  <section class="library-view">
    <header class="header">
      <h1>Tu Biblioteca</h1>
      <div class="tabs">
        <button :class="{ active: tab === 'tracks' }" @click="tab = 'tracks'">Canciones</button>
        <button :class="{ active: tab === 'albums' }" @click="tab = 'albums'">Albumes</button>
        <button type="button" @click="goToPlaylists">Playlists</button>
      </div>
    </header>

    <section v-if="tab !== 'playlists'" class="library-toolbar">
      <div class="toolbar-top-row">
        <label class="search-wrapper" for="library-search-input">
          <span class="search-icon">⌕</span>
          <input
            id="library-search-input"
            v-model="searchQuery"
            type="search"
            :placeholder="searchPlaceholder"
            autocomplete="off"
          />
        </label>

        <div class="sort-wrapper">
          <label for="library-sort-select">Orden</label>
          <select id="library-sort-select" v-model="sortBy">
            <option v-for="option in sortOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>

        <div class="view-toggle" role="group" aria-label="Modo de visualizacion">
          <button
            type="button"
            class="view-btn"
            :class="{ active: viewMode === 'list' }"
            title="Vista lista"
            aria-label="Vista lista"
            @click="viewMode = 'list'"
          >
            <span class="view-icon view-icon-list">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>
          <button
            type="button"
            class="view-btn"
            :class="{ active: viewMode === 'grid' }"
            title="Vista grid"
            aria-label="Vista grid"
            @click="viewMode = 'grid'"
          >
            <span class="view-icon view-icon-grid">
              <span></span>
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>
        </div>
      </div>
    </section>

    <p v-if="error" class="error">{{ error }}</p>

    <template v-if="tab === 'tracks'">
      <p v-if="loadingTracks">Cargando canciones...</p>
      <p v-else-if="!filteredTracks.length" class="empty">No tienes canciones guardadas.</p>
      <div v-else :class="viewMode === 'grid' ? 'grid' : 'list'">
        <article
          v-for="track in filteredTracks"
          :key="track.id"
          :class="viewMode === 'grid' ? 'card' : 'row'"
        >
          <div :class="viewMode === 'grid' ? 'card-cover' : 'row-main'">
            <img
              v-if="track.imageUrl"
              :src="track.imageUrl"
              :alt="track.name"
              :class="viewMode === 'grid' ? 'card-image' : 'row-cover'"
            />
            <div
              v-else
              :class="viewMode === 'grid' ? 'card-image card-placeholder' : 'row-cover row-cover-placeholder'"
            >
              ♪
            </div>

            <div :class="viewMode === 'grid' ? 'card-info' : 'row-info'">
              <h3 :class="viewMode === 'grid' ? 'card-title' : 'row-title'">{{ track.name }}</h3>
              <p :class="viewMode === 'grid' ? 'card-meta' : 'row-meta'">
                {{ (track.artists || []).join(', ') }} · {{ track.albumName || "Album no disponible" }}
              </p>
            </div>
          </div>
          <button
            :class="viewMode === 'grid' ? 'card-action' : 'row-action'"
            :disabled="mutating"
            @click="removeTrack(track.id)"
          >
            Quitar
          </button>
        </article>
      </div>

      <button
        v-if="tracksPage.hasNext"
        class="load-more"
        :disabled="loadingTracks"
        @click="loadMoreTracks"
      >
        Cargar mas canciones
      </button>
    </template>

    <template v-else>
      <p v-if="loadingAlbums">Cargando albumes...</p>
      <p v-else-if="!filteredAlbums.length" class="empty">No tienes albumes guardados.</p>
      <div v-else :class="viewMode === 'grid' ? 'grid' : 'list'">
        <article
          v-for="album in filteredAlbums"
          :key="album.id"
          :class="viewMode === 'grid' ? 'card' : 'row'"
        >
          <div :class="viewMode === 'grid' ? 'card-cover' : 'row-main'">
            <img
              v-if="album.imageUrl"
              :src="album.imageUrl"
              :alt="album.name"
              :class="viewMode === 'grid' ? 'card-image' : 'row-cover'"
            />
            <div
              v-else
              :class="viewMode === 'grid' ? 'card-image card-placeholder' : 'row-cover row-cover-placeholder'"
            >
              ▣
            </div>

            <div :class="viewMode === 'grid' ? 'card-info' : 'row-info'">
              <h3 :class="viewMode === 'grid' ? 'card-title' : 'row-title'">{{ album.name }}</h3>
              <p :class="viewMode === 'grid' ? 'card-meta' : 'row-meta'">
                {{ (album.artists || []).join(', ') }} ·
                {{ album.releaseDate || "Fecha no disponible" }}
              </p>
            </div>
          </div>
          <button
            :class="viewMode === 'grid' ? 'card-action' : 'row-action'"
            :disabled="mutating"
            @click="removeAlbum(album.id)"
          >
            Quitar
          </button>
        </article>
      </div>

      <button
        v-if="albumsPage.hasNext"
        class="load-more"
        :disabled="loadingAlbums"
        @click="loadMoreAlbums"
      >
        Cargar mas albumes
      </button>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useLibrary } from "@/modules/library/composables/useLibrary";

const route = useRoute();
const router = useRouter();
const tab = ref(resolveTabFromQuery(route.query.tab));
const searchQuery = ref("");
const sortBy = ref("recent");
const viewMode = ref("list");
const {
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
} = useLibrary();

onMounted(async () => {
  await loadTracks(20, 0);
  if (tab.value === "albums" && !albumsPage.value.items?.length) {
    await loadAlbums(20, 0);
  }
});

watch(tab, async (nextTab) => {
  if (nextTab === "albums" && !albumsPage.value.items?.length) {
    await loadAlbums(20, 0);
  }

  if (route.query.tab !== nextTab) {
    router.replace({ query: { ...route.query, tab: nextTab } });
  }
});

watch(
  () => route.query.tab,
  (nextValue) => {
    const nextTab = resolveTabFromQuery(nextValue);
    if (nextTab !== tab.value) {
      tab.value = nextTab;
    }
  }
);

function resolveTabFromQuery(value) {
  return value === "albums" ? "albums" : "tracks";
}

const searchPlaceholder = computed(() =>
  tab.value === "albums"
    ? "Buscar por album o artista"
    : "Buscar por cancion, artista o album"
);

const sortOptions = computed(() => {
  if (tab.value === "albums") {
    return [
      { value: "recent", label: "Recientes primero" },
      { value: "name-asc", label: "Nombre A-Z" },
      { value: "name-desc", label: "Nombre Z-A" },
      { value: "artist-asc", label: "Artista A-Z" },
      { value: "date-desc", label: "Fecha reciente" },
      { value: "date-asc", label: "Fecha antigua" },
    ];
  }

  return [
    { value: "recent", label: "Recientes primero" },
    { value: "name-asc", label: "Nombre A-Z" },
    { value: "name-desc", label: "Nombre Z-A" },
    { value: "artist-asc", label: "Artista A-Z" },
    { value: "album-asc", label: "Album A-Z" },
  ];
});

const filteredTracks = computed(() => {
  const items = tracksPage.value.items || [];
  const query = searchQuery.value.trim().toLowerCase();
  const indexed = items.map((item, index) => ({ item, index }));

  const filtered = indexed.filter(({ item }) => {
    if (!query) {
      return true;
    }
    const name = String(item?.name || "").toLowerCase();
    const artists = (item?.artists || []).join(", ").toLowerCase();
    const album = String(item?.albumName || "").toLowerCase();
    return name.includes(query) || artists.includes(query) || album.includes(query);
  });

  const sorted = filtered.slice().sort((left, right) => {
    const a = left.item;
    const b = right.item;
    switch (sortBy.value) {
      case "name-asc":
        return String(a.name || "").localeCompare(String(b.name || ""), "es");
      case "name-desc":
        return String(b.name || "").localeCompare(String(a.name || ""), "es");
      case "artist-asc":
        return String((a.artists || [""])[0]).localeCompare(String((b.artists || [""])[0]), "es");
      case "album-asc":
        return String(a.albumName || "").localeCompare(String(b.albumName || ""), "es");
      case "recent":
      default:
        return left.index - right.index;
    }
  });

  return sorted.map((entry) => entry.item);
});

const filteredAlbums = computed(() => {
  const items = albumsPage.value.items || [];
  const query = searchQuery.value.trim().toLowerCase();
  const indexed = items.map((item, index) => ({ item, index }));

  const filtered = indexed.filter(({ item }) => {
    if (!query) {
      return true;
    }
    const name = String(item?.name || "").toLowerCase();
    const artists = (item?.artists || []).join(", ").toLowerCase();
    const releaseDate = String(item?.releaseDate || "").toLowerCase();
    return name.includes(query) || artists.includes(query) || releaseDate.includes(query);
  });

  const sorted = filtered.slice().sort((left, right) => {
    const a = left.item;
    const b = right.item;
    switch (sortBy.value) {
      case "name-asc":
        return String(a.name || "").localeCompare(String(b.name || ""), "es");
      case "name-desc":
        return String(b.name || "").localeCompare(String(a.name || ""), "es");
      case "artist-asc":
        return String((a.artists || [""])[0]).localeCompare(String((b.artists || [""])[0]), "es");
      case "date-desc":
        return safeDateValue(b.releaseDate) - safeDateValue(a.releaseDate);
      case "date-asc":
        return safeDateValue(a.releaseDate) - safeDateValue(b.releaseDate);
      case "recent":
      default:
        return left.index - right.index;
    }
  });

  return sorted.map((entry) => entry.item);
});

function safeDateValue(value) {
  const dateValue = Date.parse(String(value || ""));
  return Number.isFinite(dateValue) ? dateValue : 0;
}

async function removeTrack(trackId) {
  const result = await toggleTrack(trackId, true);
  if (result?.success) {
    await loadTracks(20, 0);
  }
}

async function loadMoreTracks() {
  const nextOffset = Number(tracksPage.value.offset || 0) + Number(tracksPage.value.limit || 20);
  await loadTracks(tracksPage.value.limit || 20, nextOffset, { append: true });
}

async function loadMoreAlbums() {
  const nextOffset = Number(albumsPage.value.offset || 0) + Number(albumsPage.value.limit || 20);
  await loadAlbums(albumsPage.value.limit || 20, nextOffset, { append: true });
}

async function removeAlbum(albumId) {
  const result = await toggleAlbum(albumId, true);
  if (result?.success) {
    await loadAlbums(20, 0);
  }
}

function goToPlaylists() {
  router.push("/playlists");
}
</script>

<style scoped>
.library-view {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
}

.header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
}

.tabs {
  display: flex;
  gap: 0.5rem;
}

.tabs button {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.3rem 0.8rem;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.tabs button:hover {
  border-color: var(--color-accent);
}

.tabs .active {
  border-color: var(--color-accent);
  background: rgba(207, 163, 113, 0.16);
  color: var(--color-primary);
}

.library-toolbar {
  position: sticky;
  top: 64px;
  z-index: 20;
  background: rgba(253, 248, 239, 0.92);
  backdrop-filter: blur(7px);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 0.9rem;
  margin-bottom: 1rem;
  box-shadow: var(--app-shadow-soft);
}

.toolbar-top-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 0.75rem;
  align-items: center;
}

.search-wrapper {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 0.45rem;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: 10px;
  padding: 0.5rem 0.65rem;
  transition: border-color 0.18s;
}

.search-wrapper:focus-within {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(207, 163, 113, 0.18);
}

.search-icon {
  color: var(--color-muted-soft);
  font-size: 0.95rem;
}

.search-wrapper input {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  color: var(--color-text);
  font-size: 0.9rem;
}

.sort-wrapper {
  display: flex;
  align-items: center;
  gap: 0.45rem;
}

.sort-wrapper label {
  color: var(--color-muted);
  font-size: 0.8rem;
}

.sort-wrapper select {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  border-radius: 9px;
  padding: 0.45rem 0.55rem;
  font-size: 0.85rem;
}

.view-toggle {
  display: inline-flex;
  border: 1px solid var(--color-border);
  border-radius: 9px;
  overflow: hidden;
}

.view-btn {
  border: none;
  background: var(--color-surface);
  color: var(--color-muted);
  padding: 0.45rem 0.6rem;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.view-btn.active {
  background: var(--color-surface-strong);
  color: var(--color-primary);
}

.view-icon {
  display: inline-grid;
  width: 18px;
  height: 18px;
  gap: 3px;
}

.view-icon-list {
  grid-template-rows: repeat(3, 1fr);
}

.view-icon-list span {
  display: block;
  height: 3px;
  border-radius: 999px;
  background: currentColor;
}

.view-icon-grid {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.view-icon-grid span {
  display: block;
  border-radius: 4px;
  background: currentColor;
}

.list {
  display: grid;
  gap: 0.75rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.9rem;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 0.8rem;
  transition: border-color 0.15s;
}

.row:hover {
  border-color: var(--color-accent);
}

.row-main {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  min-width: 0;
  flex: 1 1 320px;
}

.row-cover {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--color-surface-soft);
}

.row-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  background: var(--color-surface-soft);
  border: 1px dashed var(--color-border-soft);
}

.row-info {
  min-width: 0;
}

.row-title {
  font-size: 0.95rem;
  color: var(--color-text);
  margin-bottom: 0.15rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.row-meta {
  color: var(--color-muted);
  font-size: 0.85rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.row-action {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.3rem 0.8rem;
  cursor: pointer;
  transition: border-color 0.15s;
  flex-shrink: 0;
}

.card {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 0.75rem;
  transition: border-color 0.15s, transform 0.15s;
}

.card:hover {
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.card-cover {
  display: grid;
  gap: 0.6rem;
}

.card-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 10px;
  object-fit: cover;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 1.3rem;
}

.card-info {
  min-width: 0;
}

.card-title {
  font-size: 0.95rem;
  color: var(--color-text);
  margin-bottom: 0.15rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  color: var(--color-muted);
  font-size: 0.82rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-action {
  align-self: flex-start;
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.28rem 0.75rem;
  cursor: pointer;
  transition: border-color 0.15s;
}

.card-action:hover:not(:disabled) {
  border-color: var(--color-accent-wine);
  color: var(--color-accent-wine);
}

.row-action:hover:not(:disabled) {
  border-color: var(--color-accent-wine);
  color: var(--color-accent-wine);
}

.load-more {
  margin-top: 0.8rem;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
  cursor: pointer;
  transition: border-color 0.15s;
}

.load-more:hover:not(:disabled) {
  border-color: var(--color-accent);
}

.empty { color: var(--color-muted); }
.error { color: var(--color-accent-wine); }

@media (max-width: 900px) {
  .toolbar-top-row {
    grid-template-columns: 1fr;
  }

  .sort-wrapper {
    justify-content: space-between;
  }

  .view-toggle {
    width: fit-content;
  }
}
</style>
