<template>
  <section class="followed-artists-view">
    <h1>Artistas Seguidos</h1>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Cargando artistas...</p>
    <p v-else-if="!artistsPage.items?.length" class="empty">No hay artistas seguidos para mostrar.</p>

    <div v-else class="grid">
      <article v-for="artist in artistsPage.items" :key="artist.id" class="artist-card">
        <img v-if="artist.imageUrl" :src="artist.imageUrl" :alt="artist.name" class="cover" />
        <h3>{{ artist.name }}</h3>
        <button :disabled="mutating" @click="unfollow(artist.id)">Dejar de seguir</button>
      </article>
    </div>

    <button
      v-if="artistsPage.hasNext && artistsPage.nextCursor"
      class="load-more"
      :disabled="loading"
      @click="loadMore"
    >
      Cargar mas
    </button>
  </section>
</template>

<script setup>
import { onMounted } from "vue";
import { useFollowedArtists } from "@/modules/followedArtists/composables/useFollowedArtists";

const {
  artistsPage,
  loading,
  error,
  mutating,
  loadArtists,
  toggleFollowArtist,
} = useFollowedArtists();

onMounted(async () => {
  await loadArtists(20, "");
});

async function loadMore() {
  if (!artistsPage.value.nextCursor) {
    return;
  }

  await loadArtists(20, artistsPage.value.nextCursor);
}

async function unfollow(artistId) {
  const result = await toggleFollowArtist(artistId, true);
  if (result?.success) {
    await loadArtists(20, "");
  }
}
</script>

<style scoped>
.followed-artists-view {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.followed-artists-view h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.grid {
  margin-top: 1rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 0.9rem;
}

.artist-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-md);
  padding: 0.8rem;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.artist-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--app-shadow-soft);
}

.artist-card h3 {
  font-size: 0.96rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 0.3rem;
}

.cover {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 0.7rem;
}

.artist-card p {
  color: var(--color-muted);
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
}

.artist-card button,
.load-more {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}

.artist-card button:hover:not(:disabled),
.load-more:hover:not(:disabled) {
  border-color: var(--color-accent-wine);
  color: var(--color-accent-wine);
}

.load-more {
  margin-top: 1rem;
}

.error { color: var(--color-accent-wine); }
.empty { color: var(--color-muted); }
</style>
