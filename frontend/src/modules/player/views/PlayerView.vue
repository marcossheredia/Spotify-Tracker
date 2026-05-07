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
  background: #121212;
  color: #fff;
  padding: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.state-card,
.queue-card {
  margin-top: 1rem;
  background: #1a1a1a;
  border: 1px solid #2f2f2f;
  border-radius: 12px;
  padding: 0.9rem;
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
  border: 1px solid #4b4b4b;
  background: #151515;
  color: #fff;
  border-radius: 8px;
  padding: 0.35rem 0.6rem;
}

.success { color: #9af0a8; margin-top: 0.8rem; }
.error { color: #ffb4b4; margin-top: 0.8rem; }
</style>
