<template>
  <div class="playtime-view">
    <header class="view-header">
      <h1>Tiempo Escuchado</h1>
      <p>Resumen acumulado de tu tiempo de reproduccion.</p>
    </header>

    <PlaytimeStatsCard
      :stats="playtimeStats"
      :loading="playtimeLoading"
      :error="playtimeError"
      :registered-at="user?.createdAt"
    />

    <section class="history-filters">
      <div class="history-filters__inputs">
        <label>
          Desde
          <input v-model="fromDate" type="date" />
        </label>
        <label>
          Hasta
          <input v-model="toDate" type="date" />
        </label>
        <label>
          Frecuencia
          <select v-model="granularity">
            <option value="hour">Hora</option>
            <option value="day">Dia</option>
            <option value="week">Semana</option>
            <option value="month">Mes</option>
          </select>
        </label>
      </div>

      <div class="history-filters__presets">
        <button type="button" @click="applyPresetYesterday">Ayer a hoy</button>
        <button type="button" @click="applyPresetDays(7)">7 dias</button>
        <button type="button" @click="applyPresetDays(30)">30 dias</button>
        <button type="button" @click="applyPresetAll">Todo</button>
      </div>
    </section>

    <PlaytimeHistoryChart
      :history="playtimeHistory"
      :loading="historyLoading"
      :error="historyError"
      :filters="historyFilters"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useAuthStore } from "@/stores/authStore";
import PlaytimeStatsCard from "@/modules/playback/components/PlaytimeStatsCard.vue";
import PlaytimeHistoryChart from "@/modules/playback/components/PlaytimeHistoryChart.vue";
import { usePlaytimeStats } from "@/modules/playback/composables/usePlaytimeStats";

const authStore = useAuthStore();
const user = computed(() => authStore.user);
const {
  stats: playtimeStats,
  loading: playtimeLoading,
  error: playtimeError,
  syncRecentPlaytime,
  history: playtimeHistory,
  historyLoading,
  historyError,
  loadPlaytimeHistory,
} = usePlaytimeStats();

const fromDate = ref("");
const toDate = ref("");
const granularity = ref("day");

const historyFilters = computed(() => ({
  from: fromDate.value,
  to: toDate.value,
  granularity: granularity.value,
}));

onMounted(() => {
  syncRecentPlaytime();
  applyPresetDays(30);
});

watch([fromDate, toDate, granularity], () => {
  loadPlaytimeHistory(buildHistoryParams());
});

function applyPresetYesterday() {
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);

  fromDate.value = formatDateInput(yesterday);
  toDate.value = formatDateInput(today);
  granularity.value = "hour";
}

function applyPresetDays(days) {
  const now = new Date();
  const end = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const start = new Date(end);
  start.setDate(end.getDate() - (days - 1));

  fromDate.value = formatDateInput(start);
  toDate.value = formatDateInput(end);
  granularity.value = "day";
}

function applyPresetAll() {
  fromDate.value = "";
  toDate.value = "";
  granularity.value = "month";
}

function buildHistoryParams() {
  const params = {
    granularity: granularity.value || "day",
  };

  if (fromDate.value) {
    params.from = toIsoStart(fromDate.value);
  }

  if (toDate.value) {
    params.to = toIsoEnd(toDate.value);
  }

  return params;
}

function toIsoStart(dateString) {
  const date = new Date(`${dateString}T00:00:00`);
  return date.toISOString();
}

function toIsoEnd(dateString) {
  const date = new Date(`${dateString}T23:59:59`);
  return date.toISOString();
}

function formatDateInput(date) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}
</script>

<style scoped>
.playtime-view {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.view-header {
  margin-bottom: 1.25rem;
}

.view-header h1 {
  font-size: 1.5rem;
  margin-bottom: 0.35rem;
  font-weight: 700;
  color: var(--color-text);
}

.view-header p {
  color: var(--color-muted);
}

.history-filters {
  margin-top: 1.1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-md);
  padding: 1rem;
  box-shadow: var(--app-shadow-soft);
  display: grid;
  gap: 0.8rem;
}

.history-filters__inputs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.7rem;
}

.history-filters__inputs label {
  display: grid;
  gap: 0.35rem;
  font-size: 0.8rem;
  color: var(--color-muted);
}

.history-filters__inputs input,
.history-filters__inputs select {
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border-soft);
  border-radius: 8px;
  padding: 0.45rem 0.6rem;
  color: var(--color-text);
  font-family: inherit;
}

.history-filters__presets {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.history-filters__presets button {
  border: 1px solid var(--color-border-soft);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.35rem 0.85rem;
  font-size: 0.78rem;
  cursor: pointer;
  transition: border-color 0.15s, transform 0.15s;
}

.history-filters__presets button:hover {
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .playtime-view {
    padding: 0;
  }

  .history-filters {
    padding: 0.85rem;
  }
}
</style>
