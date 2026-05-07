<template>
  <div class="top-tracks-card">
    <h3>🔥 Top canciones</h3>
    <p class="period-hint">Periodo: {{ periodLabel }}</p>

    <p v-if="loading" class="section-message">Cargando top canciones...</p>
    <p v-else-if="error" class="section-message error">{{ error }}</p>

    <ul v-else-if="tracks.length" class="track-list">
      <li v-for="track in tracks" :key="track.id" class="track-item">
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

          <p class="track-meta">
            {{ track.artistsLabel }}
            <span v-if="track.albumName"> · {{ track.albumName }}</span>
          </p>
        </div>
      </li>
    </ul>

    <p v-else class="section-message">No se encontraron top canciones.</p>
  </div>
</template>

<script setup>
defineProps({
  tracks: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: "",
  },
  periodLabel: {
    type: String,
    default: "Ultimas 4 semanas",
  },
});
</script>

<style scoped>
.top-tracks-card {
  background: var(--color-surface);
  border-radius: var(--app-radius-md);
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  box-shadow: var(--app-shadow-soft);
}

.top-tracks-card h3 {
  margin-bottom: 0.35rem;
  font-size: 1.1rem;
  color: var(--color-text);
}

.period-hint {
  color: var(--color-muted-soft);
  font-size: 0.78rem;
  margin-bottom: 0.75rem;
}

.section-message {
  color: var(--color-muted);
  font-size: 0.9rem;
}

.section-message.error {
  color: var(--color-accent-wine);
}

.track-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.track-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.55rem;
  border-radius: 10px;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border-soft);
  transition: border-color 0.15s;
}

.track-item:hover {
  border-color: var(--color-accent);
}

.track-cover {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.track-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-surface-strong);
  color: var(--color-primary);
}

.track-info {
  min-width: 0;
}

.track-name {
  color: var(--color-text);
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

a.track-name:hover {
  text-decoration: underline;
  color: var(--color-primary);
}

.track-meta {
  color: var(--color-muted);
  font-size: 0.8rem;
  margin-top: 0.18rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
