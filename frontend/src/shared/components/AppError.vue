<template>
  <div class="app-error" role="alert">
    <strong>{{ title }}</strong>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({ title: { type: String, default: "Algo ha fallado" }, message: { type: String, default: "No se ha podido completar la operación." } });
</script>

<style scoped>
.app-error { background: rgba(94, 33, 40, 0.10); border: 1px solid rgba(94, 33, 40, 0.35); border-radius: 14px; color: var(--color-accent-wine); padding: 1rem; }
.app-error strong { display: block; margin-bottom: 0.35rem; color: var(--color-text); }
</style>
