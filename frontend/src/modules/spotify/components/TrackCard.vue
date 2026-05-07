<template><article class="track-card"><img v-if="track?.imageUrl" :src="track.imageUrl" :alt="track.name" /><div v-else class="track-card__placeholder">♪</div><div><h3>{{ track?.name || "Canción desconocida" }}</h3><p>{{ artistNames }}</p></div></article></template>
<script setup>import { computed } from "vue"; const props = defineProps({ track: { type: Object, required: true } }); const artistNames = computed(() => { const artists = props.track?.artists || props.track?.artistNames || []; return Array.isArray(artists) ? artists.map((artist) => artist.name || artist).join(", ") : artists || "Artista desconocido"; });</script>
<style scoped>
.track-card { display: flex; gap: .85rem; align-items: center; padding: .85rem; border-radius: 14px; background: var(--color-surface); border: 1px solid var(--color-border-soft); transition: border-color 0.15s; }
.track-card:hover { border-color: var(--color-accent); }
img, .track-card__placeholder { width: 52px; height: 52px; border-radius: 10px; object-fit: cover; flex: 0 0 auto; }
.track-card__placeholder { display: grid; place-items: center; background: var(--color-surface-strong); color: var(--color-primary); font-weight: 800; }
h3 { font-size: .98rem; margin-bottom: .2rem; color: var(--color-text); }
p { color: var(--color-muted); font-size: .88rem; }
</style>
