<template>
  <section class="search-view">
    <header>
      <h1>Busqueda Global</h1>
      <form class="search-controls" @submit.prevent="handleSearch">
        <input
          v-model="query"
          type="search"
          placeholder="Busca tracks, artistas, albumes o playlists"
        />
        <button type="submit" class="search-button" :disabled="loading">Buscar</button>
        <div class="types">
          <label v-for="option in typeOptions" :key="option.value">
            <input v-model="selectedTypes" type="checkbox" :value="option.value" />
            {{ option.label }}
          </label>
        </div>

        <section v-if="showSuggestedArtists" class="suggested-artists">
          <div class="suggested-artists__header">
            <h2>Artistas para descubrir</h2>
            <p>Ideas rapidas para empezar a buscar.</p>
          </div>

          <div class="suggested-artists__grid">
            <button
              v-for="artist in suggestedArtists"
              :key="artist.id"
              type="button"
              class="suggested-artist-card"
              @click="searchSuggestedArtist(artist)"
            >
              <div class="suggested-artist-card__image">
                <img v-if="artist.imageUrl" :src="artist.imageUrl" :alt="artist.name" />
                <span v-else>★</span>
              </div>
              <span class="suggested-artist-card__name">{{ artist.name }}</span>
            </button>
          </div>
        </section>
      </form>
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
import { computed, ref } from "vue";
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
const suggestedArtists = ref([
  { id: "sugg-rock-1", name: "Pearl Jam" },
  { id: "sugg-rock-2", name: "Led Zeppelin" },
  { id: "sugg-rock-3", name: "Fleetwood Mac" },
  { id: "sugg-rock-4", name: "Nirvana" },
  { id: "sugg-rock-5", name: "Queen" },
  { id: "sugg-rock-6", name: "David Bowie" },
  { id: "sugg-rock-7", name: "Radiohead" },
  { id: "sugg-rock-8", name: "Pink Floyd" },
]);

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

const hasQuery = computed(() => query.value.trim().length > 0);
const showSuggestedArtists = computed(
  () => !hasQuery.value && !loading.value && suggestedArtists.value.length > 0
);

function handleSearch() {
  if (!selectedTypes.value.length) {
    return;
  }
  const types = selectedTypes.value.join(",");
  executeSearch(query.value, types, 10);
}

function searchSuggestedArtist(artist) {
  query.value = artist.name;
  selectedTypes.value = ["artist"];
  executeSearch(artist.name, "artist", 10);
}
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

.suggested-artists {
  margin-top: 0.6rem;
  padding: 0.85rem;
  border-radius: 12px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  box-shadow: var(--app-shadow-soft);
}

.suggested-artists__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.suggested-artists__header h2 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-text);
}

.suggested-artists__header p {
  font-size: 0.8rem;
  color: var(--color-muted);
}

.suggested-artists__grid {
  margin-top: 0.7rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 0.7rem;
}

.suggested-artist-card {
  border: 1px solid var(--color-border-soft);
  background: var(--color-surface-soft);
  border-radius: 12px;
  padding: 0.7rem 0.6rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.45rem;
  cursor: pointer;
  color: var(--color-text);
  transition: border-color 0.15s, transform 0.15s;
}

.suggested-artist-card:hover {
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.suggested-artist-card__image {
  width: 64px;
  height: 64px;
  border-radius: 999px;
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.suggested-artist-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.suggested-artist-card__image span {
  color: var(--color-primary);
  font-size: 1.1rem;
}

.suggested-artist-card__name {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--color-text);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
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
