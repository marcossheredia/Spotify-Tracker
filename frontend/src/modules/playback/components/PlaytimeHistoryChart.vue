<template>
  <section class="history-card">
    <header class="history-header">
      <div>
        <h3>Historico de escucha</h3>
        <p class="history-subtitle">
          {{ formatRange(history?.from, history?.to) }} · {{ history?.granularity || "day" }}
        </p>
      </div>
      <div class="history-totals">
        <div>
          <span class="history-total-label">Total</span>
          <strong class="history-total-value">{{ formatDuration(history?.totalPlaytimeMs) }}</strong>
        </div>
        <div>
          <span class="history-total-label">Reproducciones</span>
          <strong class="history-total-value">{{ history?.totalReproducciones || 0 }}</strong>
        </div>
      </div>
    </header>

    <p v-if="loading" class="history-status">Cargando historico...</p>
    <p v-else-if="error" class="history-error">{{ error }}</p>
    <p v-else-if="!safePoints.length" class="history-status">
      No hay datos para este rango.
    </p>
    <p v-else-if="allZeroPoints" class="history-status">
      Aún no hay suficientes datos históricos. Se irán generando con cada sincronización.
    </p>

    <div v-else-if="!allZeroPoints" class="history-chart">
      <div class="history-chart-bars">
        <div
          v-for="point in safePoints"
          :key="point.periodStart"
          class="history-bar"
          :title="formatTooltip(point)"
        >
          <span class="history-bar-fill" :style="{ height: barHeight(point) }" />
          <span class="history-bar-label">{{ formatPointLabel(point.periodStart) }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  history: {
    type: Object,
    default: () => ({
      from: null,
      to: null,
      granularity: "day",
      totalPlaytimeMs: 0,
      totalReproducciones: 0,
      points: [],
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
  filters: {
    type: Object,
    default: () => ({}),
  },
});

const safePoints = computed(() => props.history?.points || []);
const maxPlaytime = computed(() => {
  if (!safePoints.value.length) {
    return 0;
  }
  return Math.max(...safePoints.value.map((point) => Number(point.totalPlaytimeMs || 0)));
});
const allZeroPoints = computed(() => safePoints.value.every((point) => Number(point.totalPlaytimeMs || 0) <= 0));

function barHeight(point) {
  const max = maxPlaytime.value || 1;
  const value = Number(point.totalPlaytimeMs || 0);
  const percent = Math.max(4, Math.round((value / max) * 100));
  return `${percent}%`;
}

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

function formatDate(value, options) {
  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return "-";
  }

  return parsedDate.toLocaleDateString("es-ES", options);
}

function formatRange(from, to) {
  if (!from && !to) {
    return "Todo el tiempo";
  }

  const fromLabel = from ? formatDate(from, { day: "2-digit", month: "short" }) : "Inicio";
  const toLabel = to ? formatDate(to, { day: "2-digit", month: "short" }) : "Hoy";
  return `${fromLabel} - ${toLabel}`;
}

function formatPointLabel(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "-";
  }

  if (props.history?.granularity === "week") {
    return `Sem ${date.toLocaleDateString("es-ES", { day: "2-digit", month: "short" })}`;
  }
  if (props.history?.granularity === "month") {
    return date.toLocaleDateString("es-ES", { month: "short", year: "2-digit" });
  }

  return date.toLocaleDateString("es-ES", { day: "2-digit", month: "short" });
}

function formatTooltip(point) {
  const date = new Date(point.periodStart);
  const dateLabel = Number.isNaN(date.getTime())
    ? "-"
    : date.toLocaleString("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
  return `${dateLabel} · ${formatDuration(point.totalPlaytimeMs)} · ${point.totalReproducciones || 0} reps`;
}
</script>

<style scoped>
.history-card {
  margin-top: 1.25rem;
  background: var(--color-surface);
  border-radius: var(--app-radius-md);
  padding: 1.25rem;
  border: 1px solid var(--color-border);
  box-shadow: var(--app-shadow-soft);
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 0.75rem;
}

.history-header h3 {
  font-size: 1.05rem;
  color: var(--color-text);
}

.history-subtitle {
  font-size: 0.82rem;
  color: var(--color-muted);
  margin-top: 0.2rem;
}

.history-totals {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.history-total-label {
  display: block;
  font-size: 0.7rem;
  color: var(--color-muted);
}

.history-total-value {
  font-size: 0.95rem;
  color: var(--color-text);
  font-weight: 700;
}

.history-status {
  color: var(--color-muted);
  font-size: 0.9rem;
}

.history-error {
  color: var(--color-accent-wine);
  font-size: 0.9rem;
}

.history-chart {
  margin-top: 0.75rem;
}

.history-chart-bars {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(38px, 1fr);
  gap: 0.5rem;
  align-items: end;
  min-height: 180px;
  padding-bottom: 0.35rem;
  border-bottom: 1px solid var(--color-border-soft);
  overflow-x: auto;
}

.history-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 0.35rem;
  min-width: 38px;
}

.history-bar-fill {
  width: 100%;
  background: linear-gradient(180deg, var(--color-accent), var(--color-primary));
  border-radius: 10px 10px 6px 6px;
  min-height: 6px;
  transition: height 0.2s ease;
}

.history-bar-label {
  font-size: 0.7rem;
  color: var(--color-muted);
  text-align: center;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .history-card {
    padding: 1rem;
  }

  .history-chart-bars {
    min-height: 160px;
  }
}
</style>
