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
        <p>{{ artist.followersTotal ?? "?" }} seguidores</p>
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
  background: #121212;
  color: #fff;
  padding: 2rem;
}

.grid {
  margin-top: 1rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 0.9rem;
}

.artist-card {
  background: #1c1c1c;
  border: 1px solid #303030;
  border-radius: 12px;
  padding: 0.8rem;
}

.cover {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 0.7rem;
}

.artist-card p {
  color: #b6b6b6;
  margin-bottom: 0.5rem;
}

.artist-card button,
.load-more {
  border: 1px solid #4e4e4e;
  background: transparent;
  color: #fff;
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
}

.load-more {
  margin-top: 1rem;
}

.error { color: #ffb4b4; }
.empty { color: #bbbbbb; }
</style>
