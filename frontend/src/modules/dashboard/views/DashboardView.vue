<template>
  <div class="dashboard">
    

    <div class="dashboard-now-playing">
      <NowPlayingDropdownCard
        :track="nowPlayingTrack"
        :loading="nowPlayingLoading"
        :error="nowPlayingError"
      />
    </div>

    <TopStatsPeriodSelector v-model="selectedTopStatsPeriod" />

    <div class="dashboard-body">
      <TopTracksCard
        :tracks="topTracks"
        :loading="topTracksLoading"
        :error="topTracksError"
        :period-label="selectedTopStatsPeriodLabel"
      />
      <TopArtistsCard
        :artists="topArtists"
        :loading="topArtistsLoading"
        :error="topArtistsError"
        :period-label="selectedTopStatsPeriodLabel"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import TopTracksCard from "@/modules/topTracks/components/TopTracksCard.vue";
import { useTopTracks } from "@/modules/topTracks/composables/useTopTracks";
import TopArtistsCard from "@/modules/topArtists/components/TopArtistsCard.vue";
import { useTopArtists } from "@/modules/topArtists/composables/useTopArtists";
import TopStatsPeriodSelector from "@/modules/topStats/components/TopStatsPeriodSelector.vue";
import { TopStatsPeriod } from "@/modules/topStats/domain/TopStatsPeriod";
import NowPlayingDropdownCard from "@/modules/nowPlaying/components/NowPlayingDropdownCard.vue";
import { useNowPlaying } from "@/modules/nowPlaying/composables/useNowPlaying";

const {
  tracks: topTracks,
  loading: topTracksLoading,
  error: topTracksError,
  loadTopTracks,
} = useTopTracks();
const {
  artists: topArtists,
  loading: topArtistsLoading,
  error: topArtistsError,
  loadTopArtists,
} = useTopArtists();
const {
  currentTrack: nowPlayingTrack,
  loading: nowPlayingLoading,
  error: nowPlayingError,
  loadCurrentTrack,
} = useNowPlaying();
const selectedTopStatsPeriod = ref(TopStatsPeriod.SHORT_TERM);
const NOW_PLAYING_FAST_REFRESH_MS = 5000;
const NOW_PLAYING_SLOW_REFRESH_MS = 15000;

let nowPlayingRefreshTimeoutId = null;
const selectedTopStatsPeriodLabel = computed(() =>
  TopStatsPeriod.labelFor(selectedTopStatsPeriod.value)
);

function loadTopStats() {
  loadTopTracks(5, selectedTopStatsPeriod.value);
  loadTopArtists(5, selectedTopStatsPeriod.value);
}

function refreshNowPlayingIfVisible() {
  if (typeof document !== "undefined" && document.hidden) {
    return;
  }

  loadCurrentTrack({ silent: true }).finally(() => {
    scheduleNowPlayingRefresh();
  });
}

function scheduleNowPlayingRefresh() {
  if (typeof window === "undefined") {
    return;
  }

  if (typeof document !== "undefined" && document.hidden) {
    clearNowPlayingRefresh();
    return;
  }

  clearNowPlayingRefresh();
  const interval = nowPlayingTrack.value?.isPlaying
    ? NOW_PLAYING_FAST_REFRESH_MS
    : NOW_PLAYING_SLOW_REFRESH_MS;

  nowPlayingRefreshTimeoutId = window.setTimeout(() => {
    refreshNowPlayingIfVisible();
  }, interval);
}

function clearNowPlayingRefresh() {
  if (nowPlayingRefreshTimeoutId != null) {
    window.clearTimeout(nowPlayingRefreshTimeoutId);
    nowPlayingRefreshTimeoutId = null;
  }
}

function handleVisibilityChange() {
  if (typeof document === "undefined") {
    return;
  }
  if (!document.hidden) {
    refreshNowPlayingIfVisible();
  } else {
    clearNowPlayingRefresh();
  }
}

onMounted(() => {
  loadTopStats();
  loadCurrentTrack();
  scheduleNowPlayingRefresh();

  if (typeof document !== "undefined") {
    document.addEventListener("visibilitychange", handleVisibilityChange);
  }
});

watch(selectedTopStatsPeriod, () => {
  loadTopStats();
});

onUnmounted(() => {
  clearNowPlayingRefresh();

  if (typeof document !== "undefined") {
    document.removeEventListener("visibilitychange", handleVisibilityChange);
  }
});
</script>

<style scoped>
.dashboard {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.dashboard-now-playing {
  margin-bottom: 1.5rem;
}

.dashboard-body {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}
</style>
