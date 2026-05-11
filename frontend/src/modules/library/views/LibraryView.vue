<template>
  <section class="library-view">
    <header class="header">
      <h1>Tu Biblioteca</h1>
      <div class="tabs">
        <button :class="{ active: tab === 'tracks' }" @click="tab = 'tracks'">Canciones</button>
        <button :class="{ active: tab === 'albums' }" @click="tab = 'albums'">Albumes</button>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <template v-if="tab === 'tracks'">
      <p v-if="loadingTracks">Cargando canciones...</p>
      <p v-else-if="!tracksPage.items?.length" class="empty">No tienes canciones guardadas.</p>
      <div v-else class="list">
        <article v-for="track in tracksPage.items" :key="track.id" class="row">
          <div class="row-main">
            <img
              v-if="track.imageUrl"
              :src="track.imageUrl"
              :alt="track.name"
              class="row-cover"
            />
            <div v-else class="row-cover row-cover-placeholder">♪</div>

            <div class="row-info">
              <h3 class="row-title">{{ track.name }}</h3>
              <p class="row-meta">
                {{ (track.artists || []).join(', ') }} · {{ track.albumName || "Album no disponible" }}
              </p>
            </div>
          </div>
          <button class="row-action" :disabled="mutating" @click="removeTrack(track.id)">Quitar</button>
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
      <p v-else-if="!albumsPage.items?.length" class="empty">No tienes albumes guardados.</p>
      <div v-else class="list">
        <article v-for="album in albumsPage.items" :key="album.id" class="row">
          <div class="row-main">
            <img
              v-if="album.imageUrl"
              :src="album.imageUrl"
              :alt="album.name"
              class="row-cover"
            />
            <div v-else class="row-cover row-cover-placeholder">▣</div>

            <div class="row-info">
              <h3 class="row-title">{{ album.name }}</h3>
              <p class="row-meta">
                {{ (album.artists || []).join(', ') }} ·
                {{ album.releaseDate || "Fecha no disponible" }}
              </p>
            </div>
          </div>
          <button class="row-action" :disabled="mutating" @click="removeAlbum(album.id)">Quitar</button>
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
import { onMounted, ref, watch } from "vue";
import { useLibrary } from "@/modules/library/composables/useLibrary";

const tab = ref("tracks");
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
});

watch(tab, async (nextTab) => {
  if (nextTab === "albums" && !albumsPage.value.items?.length) {
    await loadAlbums(20, 0);
  }
});

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

.list {
  display: grid;
  gap: 0.75rem;
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
</style>
