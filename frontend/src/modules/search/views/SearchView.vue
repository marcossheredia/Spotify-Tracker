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
      <div class="result-grid">
        <article v-for="track in results.tracks" :key="track.id" class="result-card">
          <div class="result-cover">
            <img v-if="track.imageUrl" :src="track.imageUrl" :alt="track.name" />
            <div v-else class="result-cover-placeholder">♪</div>
          </div>
          <div class="result-body">
            <a
              v-if="track.externalUrl"
              :href="track.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="result-title"
            >
              {{ track.name }}
            </a>
            <p v-else class="result-title">{{ track.name }}</p>
            <p class="result-meta">{{ (track.artists || []).join(', ') }}</p>
            <p v-if="track.albumName" class="result-meta">Album: {{ track.albumName }}</p>
            <p v-if="track.popularity != null" class="result-meta">
              Popularidad: {{ track.popularity }}
            </p>
          </div>
        </article>
      </div>
    </div>

    <div v-if="results.artists?.length" class="group">
      <h2>Artistas</h2>
      <div class="result-grid">
        <article v-for="artist in results.artists" :key="artist.id" class="result-card">
          <div class="result-cover is-round">
            <img v-if="artist.imageUrl" :src="artist.imageUrl" :alt="artist.name" />
            <div v-else class="result-cover-placeholder">★</div>
          </div>
          <div class="result-body">
            <a
              v-if="artist.externalUrl"
              :href="artist.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="result-title"
            >
              {{ artist.name }}
            </a>
            <p v-else class="result-title">{{ artist.name }}</p>
            <p v-if="artist.genres?.length" class="result-meta">
              {{ artist.genres.join(', ') }}
            </p>
            <p v-if="artist.followersTotal != null" class="result-meta">
              Seguidores: {{ artist.followersTotal }}
            </p>
            <p v-if="artist.popularity != null" class="result-meta">
              Popularidad: {{ artist.popularity }}
            </p>
          </div>
        </article>
      </div>
    </div>

    <div v-if="results.albums?.length" class="group">
      <h2>Albumes</h2>
      <div class="result-grid">
        <article v-for="album in results.albums" :key="album.id" class="result-card">
          <div class="result-cover">
            <img v-if="album.imageUrl" :src="album.imageUrl" :alt="album.name" />
            <div v-else class="result-cover-placeholder">▣</div>
          </div>
          <div class="result-body">
            <a
              v-if="album.externalUrl"
              :href="album.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="result-title"
            >
              {{ album.name }}
            </a>
            <p v-else class="result-title">{{ album.name }}</p>
            <p class="result-meta">{{ (album.artists || []).join(', ') }}</p>
            <p v-if="album.releaseDate" class="result-meta">Lanzamiento: {{ album.releaseDate }}</p>
            <p v-if="album.totalTracks != null" class="result-meta">
              Total canciones: {{ album.totalTracks }}
            </p>
            <p v-if="album.albumType" class="result-meta">Tipo: {{ album.albumType }}</p>
          </div>
        </article>
      </div>
    </div>

    <div v-if="results.playlists?.length" class="group">
      <h2>Playlists</h2>
      <div class="result-grid">
        <article v-for="playlist in results.playlists" :key="playlist.id" class="result-card">
          <div class="result-cover">
            <img v-if="playlist.imageUrl" :src="playlist.imageUrl" :alt="playlist.name" />
            <div v-else class="result-cover-placeholder">≡</div>
          </div>
          <div class="result-body">
            <a
              v-if="playlist.externalUrl"
              :href="playlist.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="result-title"
            >
              {{ playlist.name }}
            </a>
            <p v-else class="result-title">{{ playlist.name }}</p>
            <p v-if="playlist.ownerName" class="result-meta">Owner: {{ playlist.ownerName }}</p>
            <p v-if="playlist.tracksTotal != null" class="result-meta">
              Canciones: {{ playlist.tracksTotal }}
            </p>
            <p v-if="playlist.publicPlaylist != null" class="result-meta">
              {{ playlist.publicPlaylist ? "Publica" : "Privada" }}
              <span v-if="playlist.collaborative"> · Colaborativa</span>
            </p>
            <p v-else-if="playlist.collaborative" class="result-meta">Colaborativa</p>
          </div>
        </article>
      </div>
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

.result-grid {
  margin-top: 0.75rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.8rem;
}

.result-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.7rem;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border-soft);
  border-radius: 12px;
  transition: border-color 0.15s, transform 0.15s;
}

.result-card:hover {
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.result-cover {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--color-surface-strong);
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-cover.is-round {
  border-radius: 999px;
}

.result-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-cover-placeholder {
  color: var(--color-primary);
  font-size: 1.1rem;
}

.result-body {
  min-width: 0;
}

.result-title {
  color: var(--color-text);
  font-size: 0.95rem;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

a.result-title:hover {
  text-decoration: underline;
  color: var(--color-primary);
}

.result-meta {
  color: var(--color-muted);
  font-size: 0.8rem;
  margin-top: 0.2rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading { color: var(--color-muted); margin-top: 1rem; }
.error { color: var(--color-accent-wine); margin-top: 1rem; }
.empty { color: var(--color-muted); margin-top: 1rem; }
</style>
