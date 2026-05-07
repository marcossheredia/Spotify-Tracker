<template>
  <section class="playlist-list-shell">
    <article
      v-for="playlist in playlists"
      :key="playlist.id"
      class="playlist-item"
      :class="{ active: activePlaylistId === playlist.id }"
      @mouseenter="emit('preview', playlist)"
    >
      <button type="button" class="item-main" @click="emit('select', playlist)">
        <img
          v-if="playlist.imageUrl"
          :src="playlist.imageUrl"
          :alt="playlist.name"
          class="playlist-cover"
        />
        <div v-else class="playlist-cover playlist-cover-placeholder">♪</div>

        <div class="playlist-info">
          <p class="playlist-name">{{ playlist.name }}</p>
          <p class="playlist-meta">
            {{ playlist.tracksTotal ?? 0 }} canciones · {{ playlist.ownerName || "Sin owner" }}
          </p>
          <p v-if="playlist.lastPlayedAt" class="playlist-meta">
            Ultima reproduccion: {{ formatPlayedAt(playlist.lastPlayedAt) }}
          </p>

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
        </div>
      </button>

      <div class="item-actions">
        <button type="button" class="detail-btn" @click="emit('select', playlist)">Ver detalle</button>
        <a
          v-if="playlist.externalUrl"
          :href="playlist.externalUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="spotify-link"
        >
          Abrir Spotify
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
.playlist-list-shell {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.playlist-item {
  border: 1px solid #303030;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 0.55rem;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.65rem;
  transition: border-color 0.2s ease;
}

.playlist-item:hover {
  border-color: #4d4d4d;
}

.playlist-item.active {
  border-color: var(--color-accent, #1db954);
}

.item-main {
  border: none;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.playlist-cover {
  width: 58px;
  height: 58px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.playlist-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b3b3b;
}

.playlist-info {
  min-width: 0;
}

.playlist-name {
  color: #fff;
  font-size: 0.94rem;
  font-weight: 600;
  line-height: 1.2;
}

.playlist-meta {
  color: #b3b3b3;
  font-size: 0.78rem;
  margin-top: 0.12rem;
}

.badge-row {
  margin-top: 0.4rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
}

.badge {
  font-size: 0.68rem;
  border: 1px solid #4b4b4b;
  color: #cfcfcf;
  border-radius: 999px;
  padding: 0.14rem 0.42rem;
}

.badge-liked {
  border-color: var(--color-accent, #1db954);
  color: #fff;
}

.preview-text {
  margin-top: 0.4rem;
  color: #a5a5a5;
  font-size: 0.74rem;
}

.item-actions {
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
}

.detail-btn {
  border: 1px solid #4b4b4b;
  background: #242424;
  color: #fff;
  border-radius: 8px;
  padding: 0.35rem 0.58rem;
  font-size: 0.76rem;
  cursor: pointer;
}

.spotify-link {
  color: #bdbdbd;
  text-decoration: none;
  font-size: 0.74rem;
}

.spotify-link:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .playlist-item {
    grid-template-columns: 1fr;
  }

  .item-actions {
    justify-content: flex-end;
  }
}
</style>