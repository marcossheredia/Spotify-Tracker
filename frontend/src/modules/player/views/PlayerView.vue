<template>
  <section class="player-view">
    <header class="header">
      <h1>Estado del Reproductor</h1>
      <button :disabled="loading" @click="refresh">Actualizar</button>
    </header>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="actionMessage" class="success">{{ actionMessage }}</p>

    <article v-if="state" class="state-card">
      <h2>{{ state.currentTrack?.name || "Sin reproduccion activa" }}</h2>
      <p>{{ state.currentTrack?.artists?.join(', ') || "" }}</p>
      <p>{{ state.capabilitiesNote }}</p>
      <p v-if="state.activeDevice">Dispositivo activo: {{ state.activeDevice.name }}</p>
      <p v-else>Sin dispositivo activo.</p>

      <div class="controls">
        <button :disabled="actionLoading || !state.canControlPlayback" @click="actions.previous">Prev</button>
        <button :disabled="actionLoading || !state.canControlPlayback" @click="playOrPause">
          {{ state.isPlaying ? "Pause" : "Play" }}
        </button>
        <button :disabled="actionLoading || !state.canControlPlayback" @click="actions.next">Next</button>
      </div>

      <div class="settings">
        <button :disabled="actionLoading || !state.canControlPlayback" @click="actions.setRepeat('off')">Repeat off</button>
        <button :disabled="actionLoading || !state.canControlPlayback" @click="actions.setRepeat('context')">Repeat context</button>
        <button :disabled="actionLoading || !state.canControlPlayback" @click="toggleShuffle">
          Shuffle {{ state.shuffleState ? "on" : "off" }}
        </button>
      </div>

      <div class="transfer">
        <label for="device">Transfer playback</label>
        <select id="device" v-model="selectedDeviceId">
          <option value="">Selecciona dispositivo</option>
          <option v-for="device in state.devices || []" :key="device.id" :value="device.id">
            {{ device.name }} ({{ device.type }})
          </option>
        </select>
        <button :disabled="actionLoading || !selectedDeviceId" @click="transfer">Transferir</button>
      </div>
    </article>

    <article class="queue-card">
      <h2>Cola</h2>
      <p v-if="!queue.queue?.length">No hay cola disponible.</p>
      <ul v-else>
        <li v-for="item in queue.queue" :key="item.id">
          {{ item.name }} · {{ item.artists?.join(', ') }}
        </li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
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

.controls,
.settings,
.transfer {
  display: flex;
  gap: 0.6rem;
  margin-top: 0.8rem;
  flex-wrap: wrap;
}

button,
select {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 8px;
  padding: 0.35rem 0.6rem;
  cursor: pointer;
  transition: border-color 0.15s;
}

button:hover:not(:disabled) {
  border-color: var(--color-accent);
}

.success { color: var(--color-success); margin-top: 0.8rem; }
.error { color: var(--color-accent-wine); margin-top: 0.8rem; }
</style>
