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
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useAuthStore } from "@/stores/authStore";
import PlaytimeStatsCard from "@/modules/playback/components/PlaytimeStatsCard.vue";
import { usePlaytimeStats } from "@/modules/playback/composables/usePlaytimeStats";

const authStore = useAuthStore();
const user = computed(() => authStore.user);
const {
  stats: playtimeStats,
  loading: playtimeLoading,
  error: playtimeError,
  syncRecentPlaytime,
} = usePlaytimeStats();

onMounted(() => {
  syncRecentPlaytime();
});
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

@media (max-width: 768px) {
  .playtime-view {
    padding: 0;
  }
}
</style>
