<template>
  <details class="now-playing-card">
    <summary class="now-playing-summary">
      <div class="summary-main">
        <span class="summary-icon">▶</span>
        <div class="summary-text">
          <p class="summary-title">Reproduciendo ahora</p>
          <p class="summary-subtitle">{{ summarySubtitle }}</p>
        </div>
      </div>
      <span class="summary-caret">▾</span>
    </summary>

    <div class="now-playing-content">
      <p v-if="loading" class="section-message">Cargando reproduccion actual...</p>
      <p v-else-if="error" class="section-message error">{{ error }}</p>
      <p v-else-if="!track" class="section-message">No hay ninguna cancion sonando ahora mismo.</p>

      <div v-else class="track-row">
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
        <div class="progress-line">
          <span class="progress-fill" :style="{ width: `${track.progressPercent}%` }" />
        </div>
        <p class="progress-label">{{ track.progressLabel }}</p>
      </div>
    </div>
  </details>
</template>

<script setup>
import { computed } from "vue";

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
</script>

<style scoped>
.now-playing-card {
  background: #282828;
  border: 1px solid #383838;
  border-radius: 12px;
  padding: 0.8rem 1rem;
}

.now-playing-summary {
  list-style: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.now-playing-summary::-webkit-details-marker {
  display: none;
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
  background: var(--color-accent, #1db954);
  color: #0d0d0d;
  font-weight: 700;
  flex-shrink: 0;
}

.summary-text {
  min-width: 0;
}

.summary-title {
  color: #fff;
  font-size: 0.95rem;
  font-weight: 700;
}

.summary-subtitle {
  color: #b3b3b3;
  font-size: 0.8rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 65vw;
}

.summary-caret {
  color: #b3b3b3;
  transition: transform 0.2s ease;
}

.now-playing-card[open] .summary-caret {
  transform: rotate(180deg);
}

.now-playing-content {
  margin-top: 0.95rem;
}

.section-message {
  color: #b3b3b3;
  font-size: 0.9rem;
}

.section-message.error {
  color: #ff8080;
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
  background: #3b3b3b;
  color: #fff;
}

.track-info {
  min-width: 0;
}

.track-name {
  color: #fff;
  text-decoration: none;
  font-size: 0.98rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

a.track-name:hover {
  text-decoration: underline;
}

.track-meta {
  color: #b3b3b3;
  font-size: 0.83rem;
  margin-top: 0.2rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-state {
  color: #8ce6a8;
  font-size: 0.8rem;
  margin-top: 0.2rem;
}

.progress-block {
  margin-top: 0.8rem;
}

.progress-line {
  width: 100%;
  height: 6px;
  border-radius: 999px;
  background: #3a3a3a;
  overflow: hidden;
}

.progress-fill {
  display: block;
  height: 100%;
  background: var(--color-accent, #1db954);
}

.progress-label {
  color: #b3b3b3;
  font-size: 0.78rem;
  margin-top: 0.35rem;
}
</style>
