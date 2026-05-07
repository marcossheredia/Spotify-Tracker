<template>
  <div class="top-artists-card">
    <h3>🎤 Top artistas</h3>
    <p class="period-hint">Periodo: {{ periodLabel }}</p>

    <p v-if="loading" class="section-message">Cargando top artistas...</p>
    <p v-else-if="error" class="section-message error">{{ error }}</p>

    <ul v-else-if="artists.length" class="artist-list">
      <li v-for="artist in artists" :key="artist.id" class="artist-item">
        <img
          v-if="artist.imageUrl"
          :src="artist.imageUrl"
          :alt="artist.name"
          class="artist-cover"
        />
        <div v-else class="artist-cover artist-cover-placeholder">★</div>

        <div class="artist-info">
          <a
            v-if="artist.externalUrl"
            :href="artist.externalUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="artist-name"
          >
            {{ artist.name }}
          </a>
          <p v-else class="artist-name">{{ artist.name }}</p>

          <p v-if="artist.genres.length" class="artist-meta">{{ artist.genresLabel }}</p>
        </div>
      </li>
    </ul>

    <p v-else class="section-message">No se encontraron top artistas.</p>
  </div>
</template>

<script setup>
defineProps({
  artists: {
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
.top-artists-card {
  background: #282828;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #383838;
}

.top-artists-card h3 {
  margin-bottom: 0.35rem;
  font-size: 1.1rem;
}

.period-hint {
  color: #9c9c9c;
  font-size: 0.78rem;
  margin-bottom: 0.75rem;
}

.section-message {
  color: #b3b3b3;
  font-size: 0.9rem;
}

.section-message.error {
  color: #ff8080;
}

.artist-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.artist-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.55rem;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
}

.artist-cover {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.artist-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b3b3b;
}

.artist-info {
  min-width: 0;
}

.artist-name {
  color: #fff;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

a.artist-name:hover {
  text-decoration: underline;
}

.artist-meta {
  color: #b3b3b3;
  font-size: 0.8rem;
  margin-top: 0.18rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
