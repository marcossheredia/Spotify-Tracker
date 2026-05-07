<template>
  <div class="app-empty-state">
    <div class="app-empty-state__icon">{{ icon }}</div>
    <h3>{{ title }}</h3>
    <p>{{ message }}</p>
    <slot />
  </div>
</template>

<script setup>
defineProps({ icon: { type: String, default: "🎧" }, title: { type: String, default: "Sin resultados" }, message: { type: String, default: "Todavía no hay información para mostrar." } });
</script>

<style scoped>
.app-empty-state { text-align: center; padding: 2rem; color: var(--color-muted, #b3b3b3); }
.app-empty-state__icon { font-size: 2rem; margin-bottom: 0.75rem; }
h3 { color: #fff; margin-bottom: 0.5rem; }
</style>
