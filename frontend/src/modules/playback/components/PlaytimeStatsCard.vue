<template>
  <div class="playtime-card">
    <h3>⏱️ Tiempo escuchado</h3>

    <p v-if="loading" class="section-message">Sincronizando reproducciones...</p>
    <p v-else-if="error" class="section-message error">{{ error }}</p>

    <div v-else class="stats-grid">
      <div class="stats-item">
        <span class="stats-label">Total acumulado</span>
        <strong class="stats-value">{{ formatDuration(stats.totalPlaytimeMs) }}</strong>
      </div>

      <div class="stats-item">
        <span class="stats-label">Acumulado desde</span>
        <strong class="stats-value">{{ formatDate(registeredAt) }}</strong>
      </div>

      <div class="stats-item">
        <span class="stats-label">Anadido en ultima sync</span>
        <strong class="stats-value">{{ formatDuration(stats.addedDurationMs) }}</strong>
      </div>

      <div class="stats-item">
        <span class="stats-label">Nuevas reproducciones</span>
        <strong class="stats-value">{{ stats.addedTracks || 0 }}</strong>
      </div>

      <div class="stats-item">
        <span class="stats-label">Total reproducciones</span>
        <strong class="stats-value">{{ stats.totalReproducciones || 0 }}</strong>
      </div>

      <div v-if="stats.lastSyncAt" class="stats-item full-width">
        <span class="stats-label">Ultima sincronizacion</span>
        <strong class="stats-value">{{ formatDateTime(stats.lastSyncAt) }}</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  stats: {
    type: Object,
    default: () => ({
      addedTracks: 0,
      addedDurationMs: 0,
      totalPlaytimeMs: 0,
      totalReproducciones: 0,
      lastSyncAt: null,
    }),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: "",
  },
  registeredAt: {
    type: [String, Number, Date],
    default: null,
  },
});

function formatDuration(durationMs) {
  const safeMs = Number(durationMs || 0);
  const totalMinutes = Math.floor(safeMs / 60000);

  if (safeMs < 3600000) {
    return `${totalMinutes} min`;
  }

  if (safeMs < 86400000) {
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return `${hours} h ${minutes} min`;
  }

  const totalHours = Math.floor(totalMinutes / 60);
  const days = Math.floor(totalHours / 24);
  const hours = totalHours % 24;
  return `${days} d ${hours} h`;
}

function formatDateTime(value) {
  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return "-";
  }

  return parsedDate.toLocaleString("es-ES", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDate(value) {
  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return "No disponible";
  }

  return parsedDate.toLocaleDateString("es-ES", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}
</script>

<style scoped>
.playtime-card {
  background: #282828;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #383838;
}

.playtime-card h3 {
  margin-bottom: 0.8rem;
  font-size: 1.1rem;
}

.section-message {
  color: #b3b3b3;
  font-size: 0.9rem;
}

.section-message.error {
  color: #ff8080;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.stats-item {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  padding: 0.65rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stats-item.full-width {
  grid-column: 1 / -1;
}

.stats-label {
  color: #b3b3b3;
  font-size: 0.78rem;
}

.stats-value {
  color: #fff;
  font-size: 1rem;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
