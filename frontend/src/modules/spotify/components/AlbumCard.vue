<template><article class="album-card"><img v-if="album?.imageUrl" :src="album.imageUrl" :alt="album.name" /><div v-else class="album-card__placeholder">▣</div><h3>{{ album?.name || "Álbum desconocido" }}</h3><p>{{ album?.artistName || album?.artists?.map?.((artist) => artist.name || artist).join(", ") || "Spotify" }}</p></article></template>
<script setup>defineProps({ album: { type: Object, required: true } });</script>
<style scoped>
.album-card { padding: 1rem; border-radius: 16px; background: var(--color-surface); border: 1px solid var(--color-border-soft); transition: border-color 0.15s, box-shadow 0.15s; }
.album-card:hover { border-color: var(--color-accent); box-shadow: var(--app-shadow-soft); }
img, .album-card__placeholder { width: 100%; aspect-ratio: 1; border-radius: 14px; object-fit: cover; margin-bottom: .75rem; }
.album-card__placeholder { display: grid; place-items: center; background: var(--color-surface-strong); color: var(--color-primary); font-weight: 800; }
h3 { font-size: .98rem; margin-bottom: .25rem; color: var(--color-text); }
p { color: var(--color-muted); font-size: .85rem; }
</style>
