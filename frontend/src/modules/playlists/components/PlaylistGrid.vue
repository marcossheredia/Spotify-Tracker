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
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.playlist-card:hover {
  border-color: var(--color-accent);
  transform: translateY(-1px);
  box-shadow: var(--app-shadow-soft);
}

.playlist-card.active {
  border-color: var(--color-accent);
  background: rgba(207, 163, 113, 0.06);
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
  background: var(--color-surface-strong);
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
  color: var(--color-primary);
  font-size: 1.2rem;
}

.name {
  color: var(--color-text);
  font-size: 0.96rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.meta {
  color: var(--color-muted);
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
  border: 1px solid rgba(207, 163, 113, 0.35);
  color: var(--color-primary);
  border-radius: 999px;
  padding: 0.15rem 0.45rem;
  background: rgba(207, 163, 113, 0.12);
}

.badge-liked {
  border-color: var(--color-accent-wine);
  color: var(--color-accent-wine);
  background: rgba(94, 33, 40, 0.08);
}

.preview-text {
  margin-top: 0.45rem;
  color: var(--color-muted-soft);
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
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 8px;
  padding: 0.35rem 0.6rem;
  font-size: 0.78rem;
  cursor: pointer;
  transition: border-color 0.15s;
}

.detail-btn:hover {
  border-color: var(--color-accent);
}

.spotify-link {
  font-size: 0.75rem;
  color: var(--color-muted);
  text-decoration: none;
}

.spotify-link:hover {
  text-decoration: underline;
  color: var(--color-primary);
}
</style>
