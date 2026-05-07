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
  background:
    radial-gradient(circle at top right, rgba(207, 163, 113, 0.18), transparent 36%),
    linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  border: 1px solid rgba(233, 220, 186, 0.2);
  border-radius: var(--app-radius-md);
  padding: 0.8rem 1rem;
  color: var(--color-text-inverse);
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

.now-playing-card[open] .summary-caret {
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

.progress-block {
  margin-top: 0.8rem;
}

.progress-line {
  width: 100%;
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

.progress-label {
  color: rgba(233, 220, 186, 0.6);
  font-size: 0.78rem;
  margin-top: 0.35rem;
}
</style>
