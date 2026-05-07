<template>
  <section class="playlist-grid">
    <article
      v-for="playlist in playlists"
      :key="playlist.id"
      class="playlist-card"
      :class="{ active: activePlaylistId === playlist.id }"
      @mouseenter="emit('preview', playlist)"
    >
      <button type="button" class="card-main" @click="emit('select', playlist)">
        <div class="cover-shell">
          <img
            v-if="playlist.imageUrl"
            :src="playlist.imageUrl"
            :alt="playlist.name"
            class="cover"
          />
          <div v-else class="cover cover-placeholder">♪</div>
        </div>

        <p class="name">{{ playlist.name }}</p>
        <p class="meta">{{ playlist.tracksTotal ?? 0 }} canciones · {{ playlist.ownerName || "Sin owner" }}</p>
        <p v-if="playlist.lastPlayedAt" class="meta">Ultima: {{ formatPlayedAt(playlist.lastPlayedAt) }}</p>

        <div class="badge-row">
          <span v-if="playlist.lastPlayedAt" class="badge">Reciente</span>
          <span v-if="playlist.hasLikedTracks" class="badge badge-liked">Favoritas</span>
          <span v-if="playlist.ownPlaylist" class="badge">Propia</span>
          <span v-if="playlist.collaborative" class="badge">Colaborativa</span>
        </div>

        <p
          v-if="previewTracksByPlaylistId?.[playlist.id]?.length"
          class="preview-text"
        >
          Preview: {{ previewTracksByPlaylistId[playlist.id].join(" · ") }}
        </p>
      </button>

      <div class="card-actions">
        <button type="button" class="detail-btn" @click="emit('select', playlist)">Ver detalle</button>
        <a
          v-if="playlist.externalUrl"
          :href="playlist.externalUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="spotify-link"
        >
          Abrir en Spotify
        </a>
      </div>
    </article>
  </section>
</template>

<script setup>
import { formatPlayedAt } from "../presentation/playlistFormatters";

defineProps({
  playlists: {
    type: Array,
    default: () => [],
  },
  activePlaylistId: {
    type: String,
    default: "",
  },
  previewTracksByPlaylistId: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(["select", "preview"]);
</script>

<style scoped>
.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.9rem;
}

.playlist-card {
  background: #191919;
  border: 1px solid #2f2f2f;
  border-radius: 14px;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.playlist-card:hover {
  border-color: #4b4b4b;
  transform: translateY(-1px);
}

.playlist-card.active {
  border-color: var(--color-accent, #1db954);
}

.card-main {
  border: none;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.cover-shell {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 0.65rem;
  background: #232323;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #adadad;
  font-size: 1.2rem;
}

.name {
  color: #fff;
  font-size: 0.96rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.meta {
  color: #b3b3b3;
  font-size: 0.78rem;
  line-height: 1.35;
}

.badge-row {
  margin-top: 0.45rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}

.badge {
  font-size: 0.7rem;
  border: 1px solid #4a4a4a;
  color: #cfcfcf;
  border-radius: 999px;
  padding: 0.15rem 0.45rem;
}

.badge-liked {
  border-color: var(--color-accent, #1db954);
  color: #fff;
}

.preview-text {
  margin-top: 0.45rem;
  color: #9f9f9f;
  font-size: 0.74rem;
  line-height: 1.35;
}

.card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.6rem;
}

.detail-btn {
  border: 1px solid #4b4b4b;
  background: #222;
  color: #fff;
  border-radius: 8px;
  padding: 0.35rem 0.6rem;
  font-size: 0.78rem;
  cursor: pointer;
}

.spotify-link {
  font-size: 0.75rem;
  color: #b9b9b9;
  text-decoration: none;
}

.spotify-link:hover {
  text-decoration: underline;
}
</style>
