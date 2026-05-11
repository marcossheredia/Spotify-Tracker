<template>
  <section class="player-view">
    <header class="header">
      <h1>Estado del Reproductor</h1>
    </header>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="actionMessage" class="success">{{ actionMessage }}</p>

    <article v-if="state" class="state-card">
      <div class="state-top">
        <div class="state-heading">
          <h2>Reproduciendo ahora</h2>
          <p class="state-status">{{ state.isPlaying ? "En reproduccion" : "Pausada" }}</p>
        </div>
        <button class="refresh-button" :disabled="loading" @click="refresh">Actualizar</button>
      </div>

      <div class="state-main">
        <div class="state-cover">
          <img
            v-if="state.currentTrack?.imageUrl"
            :src="state.currentTrack.imageUrl"
            :alt="state.currentTrack.name"
          />
          <div v-else class="state-cover-placeholder">♪</div>
        </div>

        <div class="state-info">
          <p class="track-title">{{ state.currentTrack?.name || "Sin reproduccion activa" }}</p>
          <p v-if="state.currentTrack?.artists?.length" class="track-meta">
            {{ state.currentTrack.artists.join(', ') }}
          </p>
          <p v-if="state.currentTrack?.albumName" class="track-meta">
            Album: {{ state.currentTrack.albumName }}
          </p>
          <p class="device-meta" v-if="state.activeDevice">
            Dispositivo activo: {{ state.activeDevice.name }}
          </p>
          <p class="device-meta" v-else>Sin dispositivo activo.</p>
          <p class="capabilities">{{ state.capabilitiesNote }}</p>
        </div>
      </div>

      <div class="controls">
        <button
          class="control-button"
          :disabled="actionLoading || !state.canControlPlayback"
          @click="actions.previous"
        >
          ⏮
        </button>
        <button
          class="control-button primary"
          :disabled="actionLoading || !state.canControlPlayback"
          @click="playOrPause"
        >
          {{ state.isPlaying ? "⏸" : "▶" }}
        </button>
        <button
          class="control-button"
          :disabled="actionLoading || !state.canControlPlayback"
          @click="actions.next"
        >
          ⏭
        </button>
      </div>

      <div class="settings">
        <button
          class="pill"
          :disabled="actionLoading || !state.canControlPlayback"
          @click="cycleRepeat"
        >
          Repeat: {{ repeatLabel }}
        </button>
        <button
          class="pill"
          :disabled="actionLoading || !state.canControlPlayback"
          @click="toggleShuffle"
        >
          Shuffle: {{ state.shuffleState ? "on" : "off" }}
        </button>
      </div>

      <div class="transfer">
        <p class="transfer-title">Transfer playback</p>
        <div class="transfer-controls">
          <select id="device" v-model="selectedDeviceId">
            <option value="">Selecciona dispositivo</option>
            <option v-for="device in state.devices || []" :key="device.id" :value="device.id">
              {{ device.name }} ({{ device.type }})
            </option>
          </select>
          <button :disabled="actionLoading || !selectedDeviceId" @click="transfer">Transferir</button>
        </div>
      </div>
    </article>

    <article class="queue-card">
      <h2>Cola</h2>
      <p v-if="!queue.queue?.length">No hay cola disponible.</p>
      <div v-else class="queue-list">
        <div v-for="item in queue.queue" :key="item.id" class="queue-item">
          <img
            v-if="item.imageUrl"
            :src="item.imageUrl"
            :alt="item.name"
            class="queue-cover"
          />
          <div class="queue-info">
            <p class="queue-title">{{ item.name }}</p>
            <p class="queue-meta">{{ item.artists?.join(', ') }}</p>
          </div>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { usePlayer } from "@/modules/player/composables/usePlayer";

const {
  state,
  queue,
  loading,
  actionLoading,
  error,
  actionMessage,
  loadState,
  loadQueue,
  actions,
} = usePlayer();

const selectedDeviceId = ref("");
const repeatOrder = ["off", "context", "track"];

const repeatLabel = computed(() => {
  const repeatState = state.value?.repeatState || "off";
  return repeatState;
});

onMounted(async () => {
  await refresh();
});

async function refresh() {
  await loadState();
  await loadQueue();
}

async function playOrPause() {
  if (!state.value?.isPlaying) {
    await actions.play();
    return;
  }

  await actions.pause();
}

async function toggleShuffle() {
  await actions.setShuffle(!state.value?.shuffleState);
}

async function cycleRepeat() {
  const current = state.value?.repeatState || "off";
  const index = repeatOrder.indexOf(current);
  const next = repeatOrder[(index + 1) % repeatOrder.length];
  await actions.setRepeat(next);
}

async function transfer() {
  if (!selectedDeviceId.value) {
    return;
  }

  await actions.transferPlayback(selectedDeviceId.value, true);
}
</script>

<style scoped>
.player-view {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
}

.state-card,
.queue-card {
  margin-top: 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-md);
  padding: 0.9rem;
  box-shadow: var(--app-shadow-soft);
}

.state-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.8rem;
}

.state-heading h2 {
  font-size: 1.05rem;
  color: var(--color-text);
}

.state-status {
  color: var(--color-muted);
  font-size: 0.85rem;
  margin-top: 0.2rem;
}

.refresh-button {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.3rem 0.75rem;
  cursor: pointer;
  font-size: 0.8rem;
  transition: border-color 0.15s;
}

.refresh-button:hover:not(:disabled) {
  border-color: var(--color-accent);
}

.state-main {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 1rem;
  align-items: center;
}

.state-cover {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border-soft);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.state-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.state-cover-placeholder {
  font-size: 2rem;
  color: var(--color-primary);
}

.state-info {
  min-width: 0;
}

.track-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.track-meta {
  color: var(--color-muted);
  font-size: 0.88rem;
  margin-top: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.device-meta {
  color: var(--color-text);
  font-size: 0.85rem;
  margin-top: 0.35rem;
}

.capabilities {
  color: var(--color-muted);
  font-size: 0.78rem;
  margin-top: 0.35rem;
}

.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.9rem;
  margin-top: 1rem;
}

.control-button {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  cursor: pointer;
  font-size: 1.1rem;
  transition: border-color 0.15s, transform 0.15s;
}

.control-button.primary {
  width: 58px;
  height: 58px;
  font-size: 1.35rem;
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border-color: transparent;
  box-shadow: var(--app-shadow-soft);
}

.control-button:hover:not(:disabled) {
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.settings {
  display: flex;
  justify-content: center;
  gap: 0.6rem;
  margin-top: 0.9rem;
  flex-wrap: wrap;
}

.pill {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.35rem 0.85rem;
  font-size: 0.8rem;
  cursor: pointer;
  transition: border-color 0.15s;
}

.pill:hover:not(:disabled) {
  border-color: var(--color-accent);
}

.transfer {
  margin-top: 1rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border-soft);
}

.transfer-title {
  font-size: 0.85rem;
  color: var(--color-muted);
  margin-bottom: 0.5rem;
}

.transfer-controls {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.transfer select,
.transfer button {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 8px;
  padding: 0.35rem 0.6rem;
  cursor: pointer;
  transition: border-color 0.15s;
}

.transfer button:hover:not(:disabled),
.transfer select:hover:not(:disabled) {
  border-color: var(--color-accent);
}

.queue-list {
  display: grid;
  gap: 0.65rem;
  margin-top: 0.6rem;
}

.queue-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem;
  border-radius: 10px;
  border: 1px solid var(--color-border-soft);
  background: var(--color-surface-soft);
}

.queue-cover {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.queue-info {
  min-width: 0;
}

.queue-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.queue-meta {
  color: var(--color-muted);
  font-size: 0.78rem;
  margin-top: 0.2rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 720px) {
  .state-main {
    grid-template-columns: 1fr;
  }

  .state-cover {
    width: 100%;
    height: 220px;
  }
}

.success { color: var(--color-success); margin-top: 0.8rem; }
.error { color: var(--color-accent-wine); margin-top: 0.8rem; }
</style>
