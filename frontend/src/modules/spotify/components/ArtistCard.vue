<template><article class="artist-card"><img v-if="artist?.imageUrl" :src="artist.imageUrl" :alt="artist.name" /><div v-else class="artist-card__placeholder">★</div><h3>{{ artist?.name || "Artista desconocido" }}</h3><p v-if="artist?.genres?.length">{{ artist.genres.slice(0, 3).join(", ") }}</p></article></template>
<script setup>defineProps({ artist: { type: Object, required: true } });</script>
<style scoped>
.artist-card { padding: 1rem; border-radius: 16px; background: var(--color-surface); border: 1px solid var(--color-border-soft); text-align: center; transition: border-color 0.15s, box-shadow 0.15s; }
.artist-card:hover { border-color: var(--color-accent); box-shadow: var(--app-shadow-soft); }
img, .artist-card__placeholder { width: 96px; height: 96px; border-radius: 50%; object-fit: cover; margin: 0 auto .75rem; }
.artist-card__placeholder { display: grid; place-items: center; background: var(--color-surface-strong); color: var(--color-primary); font-weight: 800; }
h3 { font-size: 1rem; margin-bottom: .25rem; color: var(--color-text); }
p { color: var(--color-muted); font-size: .85rem; }
</style>
