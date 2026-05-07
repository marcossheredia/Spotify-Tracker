<template>
  <Teleport to="body">
    <div v-if="open" class="drawer-shell">
      <button type="button" class="drawer-backdrop" @click="emit('close')"></button>

      <aside class="drawer-panel" role="dialog" aria-modal="true" aria-label="Detalle playlist">
        <header class="drawer-header">
          <div class="header-main">
            <div class="header-cover-wrap">
              <img
                v-if="playlist?.imageUrl"
                :src="playlist.imageUrl"
                :alt="playlist?.name"
                class="header-cover"
              />
              <div v-else class="header-cover header-cover-placeholder">♪</div>
            </div>

            <div class="header-content">
              <p class="eyebrow">Detalle playlist</p>
              <h2>{{ playlist?.name || "Playlist" }}</h2>
              <p class="meta">Owner: {{ playlist?.ownerName || "-" }}</p>
              <p class="meta">{{ playlist?.tracksTotal ?? 0 }} canciones</p>
              <p v-if="playlist?.lastPlayedAt" class="meta">
                Ultima escucha: {{ formatPlayedAt(playlist.lastPlayedAt) }}
              </p>
            </div>
          </div>

          <button type="button" class="close-btn" @click="emit('close')">Cerrar</button>
        </header>

        <div class="drawer-content">
          <div v-if="loading" class="loading-block">
            <div class="loader-line line-a"></div>
            <div class="loader-line line-b"></div>
            <div class="loader-line line-c"></div>
          </div>

          <div v-else-if="fallbackMessage" class="fallback-card">
            <p>{{ fallbackMessage }}</p>
            <a
              v-if="playlist?.externalUrl"
              :href="playlist.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="spotify-link"
            >
              Abrir en Spotify
            </a>
          </div>

          <template v-else>
            <div class="tracks-toolbar">
              <button
                type="button"
                class="track-filter-btn"
                :class="{ active: trackFilter === 'all' }"
                @click="emit('update:trackFilter', 'all')"
              >
                Todas
              </button>
              <button
                type="button"
                class="track-filter-btn"
                :class="{ active: trackFilter === 'liked' }"
                @click="emit('update:trackFilter', 'liked')"
              >
                Solo favoritas
              </button>
              <span class="tracks-count">{{ visibleTracks.length }} pistas</span>
            </div>

            <p v-if="!visibleTracks.length" class="empty-msg">No hay pistas para este filtro.</p>

            <ul v-else class="track-list">
              <li v-for="track in visibleTracks" :key="track.id || `${track.name}-${track.albumName}`" class="track-row">
                <div class="track-main">
                  <p class="track-name">{{ track.name || "Sin titulo" }}</p>
                  <p class="track-meta">{{ track.artistsLabel }} <span v-if="track.albumName">· {{ track.albumName }}</span></p>
                </div>

                <div class="track-extra">
                  <span v-if="track.liked" class="liked-flag">❤</span>
                  <span class="duration">{{ track.durationLabel }}</span>
                  <a
                    v-if="track.externalUrl"
                    :href="track.externalUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="track-link"
                  >
                    Spotify
                  </a>
                </div>
              </li>
            </ul>
          </template>
        </div>
      </aside>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from "vue";
import { formatPlayedAt } from "../presentation/playlistFormatters";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  playlist: {
    type: Object,
    default: null,
  },
  detail: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  requestError: {
    type: String,
    default: "",
  },
  trackFilter: {
    type: String,
    default: "all",
  },
});

const emit = defineEmits(["close", "update:trackFilter"]);

const fallbackMessage = computed(() => {
  if (props.requestError) {
    return props.requestError;
  }

  if (props.detail && props.detail.canLoadTracks === false) {
    return props.detail.unavailableReason || "No se pudo cargar el detalle de la playlist.";
  }

  return "";
});

const visibleTracks = computed(() => {
  const tracks = Array.isArray(props.detail?.tracks) ? props.detail.tracks : [];

  if (props.trackFilter === "liked") {
    return tracks.filter((track) => track.liked);
  }

  return tracks;
});
</script>

<style scoped>
.drawer-shell {
  position: fixed;
  inset: 0;
  z-index: 130;
  display: flex;
  justify-content: flex-end;
}

.drawer-backdrop {
  position: absolute;
  inset: 0;
  border: none;
  background: rgba(18, 50, 55, 0.45);
}

.drawer-panel {
  position: relative;
  width: min(520px, 100%);
  height: 100%;
  background: var(--color-surface);
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  box-shadow: -8px 0 32px rgba(18, 50, 55, 0.14);
}

.drawer-header {
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
}

.header-main {
  display: flex;
  gap: 0.8rem;
  min-width: 0;
}

.header-cover-wrap {
  width: 86px;
  height: 86px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--color-surface-strong);
}

.header-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.header-content {
  min-width: 0;
}

.eyebrow {
  color: var(--color-muted-soft);
  font-size: 0.74rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.header-content h2 {
  color: var(--color-text);
  font-size: 1.1rem;
  margin: 0.2rem 0 0.45rem;
  font-weight: 700;
}

.meta {
  color: var(--color-muted);
  font-size: 0.8rem;
  margin-top: 0.15rem;
}

.close-btn {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 8px;
  padding: 0.4rem 0.65rem;
  cursor: pointer;
  height: fit-content;
  transition: border-color 0.15s;
}

.close-btn:hover {
  border-color: var(--color-accent);
}

.drawer-content {
  padding: 1rem;
  overflow-y: auto;
}

.loading-block {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.loader-line {
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--color-surface-soft), var(--color-surface-strong), var(--color-surface-soft));
  background-size: 200% 100%;
  animation: shimmer 1.2s linear infinite;
}

.line-a {
  width: 74%;
}

.line-b {
  width: 58%;
}

.line-c {
  width: 46%;
}

.fallback-card {
  border: 1px dashed var(--color-border);
  border-radius: 10px;
  background: var(--color-surface-soft);
  padding: 0.85rem;
  color: var(--color-muted);
}

.spotify-link {
  margin-top: 0.6rem;
  display: inline-block;
  color: var(--color-text);
  text-decoration: none;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 0.35rem 0.65rem;
  transition: border-color 0.15s;
}

.spotify-link:hover {
  border-color: var(--color-accent);
}

.tracks-toolbar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 0.8rem;
}

.track-filter-btn {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.3rem 0.65rem;
  font-size: 0.76rem;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.track-filter-btn.active {
  border-color: var(--color-accent);
  background: rgba(207, 163, 113, 0.16);
  color: var(--color-primary);
}

.tracks-count {
  margin-left: auto;
  color: var(--color-muted-soft);
  font-size: 0.76rem;
}

.empty-msg {
  color: var(--color-muted);
  font-size: 0.85rem;
}

.track-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.track-row {
  border: 1px solid var(--color-border-soft);
  border-radius: 10px;
  padding: 0.55rem 0.6rem;
  background: var(--color-surface-soft);
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.6rem;
  align-items: center;
  transition: border-color 0.15s;
}

.track-row:hover {
  border-color: var(--color-accent);
}

.track-name {
  color: var(--color-text);
  font-size: 0.86rem;
  font-weight: 600;
}

.track-meta {
  color: var(--color-muted);
  font-size: 0.75rem;
  margin-top: 0.15rem;
}

.track-extra {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.liked-flag {
  color: var(--color-accent-wine);
  font-size: 0.9rem;
}

.duration {
  color: var(--color-muted);
  font-size: 0.74rem;
}

.track-link {
  color: var(--color-muted);
  font-size: 0.74rem;
  text-decoration: none;
}

.track-link:hover {
  text-decoration: underline;
  color: var(--color-primary);
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 700px) {
  .drawer-panel {
    width: 100%;
  }
}
</style>
