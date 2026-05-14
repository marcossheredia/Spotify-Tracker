<template>
  <div class="now-playing-card" :class="{ open: isOpen }">
    <button
      class="now-playing-summary"
      type="button"
      :aria-expanded="isOpen.toString()"
      @click="toggleOpen"
    >
      <div class="summary-main">
        <span class="summary-icon">▶</span>
        <div class="summary-text">
          <p class="summary-title">Reproduciendo ahora</p>
          <p class="summary-subtitle">{{ summarySubtitle }}</p>
        </div>
      </div>
      <span class="summary-caret">▾</span>
    </button>

    <div v-if="isOpen" class="now-playing-content">
      <p v-if="loading" class="section-message">Cargando reproduccion actual...</p>
      <p v-else-if="error" class="section-message error">{{ error }}</p>
      <p v-else-if="!track" class="section-message">No hay ninguna cancion sonando ahora mismo.</p>

      <div v-else class="player-layout">
        <div class="player-main">
          <div class="track-row">
            <img
              v-if="track.imageUrl"
              :src="track.imageUrl"
              :alt="track.name"
              class="track-cover"
            />
            <div v-else class="track-cover track-cover-placeholder">♪</div>

            <div class="track-info">
              <a
                v-if="track.externalUrl"
                :href="track.externalUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="track-name"
              >
                {{ track.name }}
              </a>
              <p v-else class="track-name">{{ track.name }}</p>

              <p class="track-meta">{{ track.artistsLabel }}</p>
              <p v-if="track.albumName" class="track-meta">Album: {{ track.albumName }}</p>
              <p class="track-state">{{ track.isPlaying ? "En reproduccion" : "Pausada" }}</p>
            </div>
          </div>

          <div v-if="track && track.durationMs" class="progress-block">
            <div class="progress-line" :class="{ paused: !track.isPlaying }">
              <span class="progress-fill" :style="{ width: `${track.progressPercent}%` }" />
              <span
                class="progress-dot"
                :class="{ playing: track.isPlaying }"
                :style="{ left: `${track.progressPercent}%` }"
                aria-hidden="true"
              />
            </div>
            <p class="progress-label">{{ track.progressLabel }}</p>
          </div>

          <a
            v-if="track && track.externalUrl"
            :href="track.externalUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="spotify-link"
          >
            Abrir en Spotify
          </a>

          <div class="mini-controls">
            <button
              class="mini-control-button"
              :disabled="actionLoading || !state?.canControlPlayback"
              @click="goPrevious"
            >
              ⏮
            </button>
            <button
              class="mini-control-button primary"
              :disabled="actionLoading || !state?.canControlPlayback"
              @click="playOrPause"
            >
              {{ state?.isPlaying ? "⏸" : "▶" }}
            </button>
            <button
              class="mini-control-button"
              :disabled="actionLoading || !state?.canControlPlayback"
              @click="goNext"
            >
              ⏭
            </button>
          </div>

          <div class="mini-settings">
            <button
              class="mini-pill"
              :disabled="actionLoading || !state?.canControlPlayback"
              @click="cycleRepeat"
            >
              Repeat: {{ state?.repeatState || "off" }}
            </button>
            <button
              class="mini-pill"
              :disabled="actionLoading || !state?.canControlPlayback"
              @click="toggleShuffle"
            >
              Shuffle: {{ state?.shuffleState ? "on" : "off" }}
            </button>
          </div>
        </div>

        <aside class="queue-panel">
          <p class="queue-title">Cola</p>
          <p v-if="!queue.queue?.length" class="queue-meta">No hay canciones en cola.</p>
          <div v-else class="queue-list">
            <div v-for="item in queue.queue" :key="item.id" class="queue-item">
              <img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                :alt="item.name"
                class="queue-cover"
              />
              <div v-else class="queue-cover track-cover-placeholder">♪</div>
              <div class="queue-info">
                <p class="queue-title">{{ item.name }}</p>
                <p class="queue-meta">{{ item.artists?.join(", ") }}</p>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { usePlayer } from "@/modules/player/composables/usePlayer";

const props = defineProps({
  track: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: "",
  },
});

const summarySubtitle = computed(() => {
  if (props.loading) {
    return "Actualizando...";
  }

  if (props.error) {
    return "No se pudo obtener el estado de reproduccion";
  }

  if (!props.track) {
    return "Nada en reproduccion";
  }

  return `${props.track.name} - ${props.track.artistsLabel}`;
});

const { state, queue, actionLoading, actions, loadState, loadQueue } = usePlayer();
const repeatOrder = ["off", "context", "track"];
const isOpen = ref(true);

function toggleOpen() {
  isOpen.value = !isOpen.value;
}

async function refreshPlayerData() {
  await loadState();
  await loadQueue();
}

async function playOrPause() {
  if (state.value?.isPlaying) {
    await actions.pause();
  } else {
    await actions.play();
  }

  await refreshPlayerData();
}

async function goPrevious() {
  await actions.previous();
  await refreshPlayerData();
}

async function goNext() {
  await actions.next();
  await refreshPlayerData();
}

async function toggleShuffle() {
  await actions.setShuffle(!state.value?.shuffleState);
  await refreshPlayerData();
}

async function cycleRepeat() {
  const current = state.value?.repeatState || "off";
  const index = repeatOrder.indexOf(current);
  const next = repeatOrder[(index + 1) % repeatOrder.length];
  await actions.setRepeat(next);
  await refreshPlayerData();
}

onMounted(async () => {
  await refreshPlayerData();
});

watch(
  () => props.track?.id,
  async (current, previous) => {
    if (current && current !== previous) {
      await refreshPlayerData();
    }
  }
);
</script>

<style scoped>
.now-playing-card {
  background:
    radial-gradient(circle at top right, rgba(207, 163, 113, 0.18), transparent 36%),
    linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  border: 1px solid rgba(233, 220, 186, 0.2);
  border-radius: var(--app-radius-md);
  padding: 0.8rem 1rem;
  color: var(--color-text-inverse);
}

.now-playing-summary {
  border: none;
  background: transparent;
  padding: 0;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  color: inherit;
  text-align: left;
}

.summary-main {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  min-width: 0;
}

.summary-icon {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-accent);
  color: var(--color-primary);
  font-weight: 700;
  flex-shrink: 0;
}

.summary-text {
  min-width: 0;
}

.summary-title {
  color: var(--color-accent-soft);
  font-size: 0.95rem;
  font-weight: 700;
}

.summary-subtitle {
  color: rgba(233, 220, 186, 0.7);
  font-size: 0.8rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 65vw;
}

.summary-caret {
  color: rgba(233, 220, 186, 0.6);
  transition: transform 0.2s ease;
}

.now-playing-card.open .summary-caret {
  transform: rotate(180deg);
}

.now-playing-content {
  margin-top: 0.95rem;
}

.section-message {
  color: rgba(233, 220, 186, 0.75);
  font-size: 0.9rem;
}

.section-message.error {
  color: #f4a4a4;
}

.player-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 280px);
  gap: 1rem;
  align-items: start;
}

.player-main {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 0;
}

.track-row {
  display: flex;
  gap: 0.85rem;
}

.track-cover {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.track-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(233, 220, 186, 0.15);
  color: var(--color-accent-soft);
}

.track-info {
  min-width: 0;
}

.track-name {
  color: var(--color-text-inverse);
  text-decoration: none;
  font-size: 0.98rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

a.track-name:hover {
  text-decoration: underline;
  color: var(--color-accent-soft);
}

.track-meta {
  color: rgba(233, 220, 186, 0.7);
  font-size: 0.83rem;
  margin-top: 0.2rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-state {
  color: var(--color-accent);
  font-size: 0.8rem;
  margin-top: 0.2rem;
  font-weight: 600;
}

.spotify-link {
  display: inline-flex;
  margin-top: 0.75rem;
  color: var(--color-text-inverse);
  font-size: 0.82rem;
  text-decoration: none;
  font-weight: 600;
}

.spotify-link:hover {
  color: var(--color-accent-soft);
  text-decoration: underline;
}

.mini-controls {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-top: 0.35rem;
}

.mini-control-button {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(233, 220, 186, 0.35);
  background: rgba(233, 220, 186, 0.12);
  color: var(--color-text-inverse);
  cursor: pointer;
  font-size: 0.95rem;
  transition: border-color 0.2s ease, transform 0.2s ease, opacity 0.2s ease;
}

.mini-control-button.primary {
  width: 42px;
  height: 42px;
  background: var(--color-accent);
  color: var(--color-primary);
  border-color: transparent;
  font-size: 1.05rem;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
}

.mini-control-button:hover:not(:disabled) {
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.mini-control-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.mini-settings {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.35rem;
}

.mini-pill {
  border: 1px solid rgba(233, 220, 186, 0.35);
  background: rgba(233, 220, 186, 0.1);
  color: var(--color-text-inverse);
  border-radius: 999px;
  padding: 0.28rem 0.75rem;
  font-size: 0.72rem;
  cursor: pointer;
  transition: border-color 0.2s ease, opacity 0.2s ease;
}

.mini-pill:hover:not(:disabled) {
  border-color: var(--color-accent);
}

.mini-pill:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.queue-panel {
  background: rgba(233, 220, 186, 0.08);
  border: 1px solid rgba(233, 220, 186, 0.18);
  border-radius: 14px;
  padding: 0.7rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 0;
}

.queue-panel > .queue-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--color-accent-soft);
}

.queue-list {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  max-height: 240px;
  overflow-y: auto;
  padding-right: 0.2rem;
}

.queue-item {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  min-width: 0;
}

.queue-cover {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.queue-info {
  min-width: 0;
}

.queue-item .queue-title {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--color-text-inverse);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.queue-meta {
  font-size: 0.72rem;
  color: rgba(233, 220, 186, 0.65);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-block {
  margin-top: 0.8rem;
}

.progress-line {
  position: relative;
  width: 92%;
  height: 6px;
  border-radius: 999px;
  background: rgba(233, 220, 186, 0.18);
  overflow: hidden;
}

.progress-fill {
  display: block;
  height: 100%;
  background: var(--color-accent);
}

.progress-dot {
  position: absolute;
  top: 50%;
  left: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--color-accent-soft);
  border: 2px solid var(--color-accent);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
  transform: translate(-50%, -50%);
  pointer-events: none;
  transition: left 0.3s ease;
}

.progress-dot.playing {
  animation: progress-dot-pulse 1.6s ease-in-out infinite;
}

@keyframes progress-dot-pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
  }
  50% {
    transform: translate(-50%, -50%) scale(1.12);
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.3);
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
  }
}

.progress-label {
  color: rgba(233, 220, 186, 0.6);
  font-size: 0.78rem;
  margin-top: 0.35rem;
}

@media (max-width: 720px) {
  .player-layout {
    grid-template-columns: 1fr;
  }

  .queue-panel {
    margin-top: 0.4rem;
  }
}
</style>
