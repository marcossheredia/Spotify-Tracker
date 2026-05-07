<template>
  <section class="search-view">
    <header>
      <h1>Busqueda Global</h1>
      <div class="search-controls">
        <input
          v-model="query"
          type="search"
          placeholder="Busca tracks, artistas, albumes o playlists"
        />
        <div class="types">
          <label v-for="option in typeOptions" :key="option.value">
            <input v-model="selectedTypes" type="checkbox" :value="option.value" />
            {{ option.label }}
          </label>
        </div>
      </div>
    </header>

    <p v-if="loading" class="loading">Buscando...</p>
    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="showEmpty" class="empty">Sin resultados para esta busqueda.</div>

    <div v-if="results.tracks?.length" class="group">
      <h2>Tracks</h2>
      <ul>
        <li v-for="track in results.tracks" :key="track.id">
          {{ track.name }} · {{ (track.artists || []).join(', ') }}
        </li>
      </ul>
    </div>

    <div v-if="results.artists?.length" class="group">
      <h2>Artistas</h2>
      <ul>
        <li v-for="artist in results.artists" :key="artist.id">{{ artist.name }}</li>
      </ul>
    </div>

    <div v-if="results.albums?.length" class="group">
      <h2>Albumes</h2>
      <ul>
        <li v-for="album in results.albums" :key="album.id">{{ album.name }}</li>
      </ul>
    </div>

    <div v-if="results.playlists?.length" class="group">
      <h2>Playlists</h2>
      <ul>
        <li v-for="playlist in results.playlists" :key="playlist.id">{{ playlist.name }}</li>
      </ul>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useGlobalSearch } from "@/modules/search/composables/useGlobalSearch";

const typeOptions = [
  { value: "track", label: "Tracks" },
  { value: "artist", label: "Artists" },
  { value: "album", label: "Albums" },
  { value: "playlist", label: "Playlists" },
];

const query = ref("");
const selectedTypes = ref(["track", "artist", "album", "playlist"]);
const { results, loading, error, executeSearch } = useGlobalSearch();

const showEmpty = computed(() => {
  if (loading.value || !query.value.trim()) {
    return false;
  }

  return [
    results.value.tracks?.length || 0,
    results.value.artists?.length || 0,
    results.value.albums?.length || 0,
    results.value.playlists?.length || 0,
  ].every((count) => count === 0);
});

let debounceId = null;
watch([query, selectedTypes], () => {
  if (debounceId) {
    window.clearTimeout(debounceId);
  }

  debounceId = window.setTimeout(() => {
    const types = selectedTypes.value.length
      ? selectedTypes.value.join(",")
      : "track,artist,album,playlist";
    executeSearch(query.value, types, 10);
  }, 350);
});
</script>

<style scoped>
.search-view {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.search-view header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 0.35rem;
}

.search-controls {
  margin-top: 0.8rem;
  display: grid;
  gap: 0.7rem;
}

.search-controls input[type="search"] {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.7rem;
  color: var(--color-text);
  font-family: inherit;
  transition: border-color 0.18s, box-shadow 0.18s;
}

.search-controls input[type="search"]:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(207, 163, 113, 0.18);
}

.types {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
  color: var(--color-muted);
}

.group {
  margin-top: 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 0.8rem;
  box-shadow: var(--app-shadow-soft);
}

.group h2 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-text);
}

.group ul {
  margin-top: 0.4rem;
  padding-left: 1.2rem;
  color: var(--color-muted);
}

.loading { color: var(--color-muted); margin-top: 1rem; }
.error { color: var(--color-accent-wine); margin-top: 1rem; }
.empty { color: var(--color-muted); margin-top: 1rem; }
</style>
