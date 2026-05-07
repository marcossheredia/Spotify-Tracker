<template>
  <div class="app-loader" role="status" aria-live="polite">
    <span class="app-loader__spinner" aria-hidden="true"></span>
    <span>{{ message }}</span>
  </div>
</template>

<script setup>
defineProps({ message: { type: String, default: "Cargando..." } });
</script>

<style scoped>
.app-loader { display: inline-flex; align-items: center; gap: 0.75rem; color: var(--color-muted, #b3b3b3); }
.app-loader__spinner { width: 1rem; height: 1rem; border: 2px solid var(--color-muted, #b3b3b3); border-right-color: transparent; border-radius: 50%; animation: spin 0.75s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
