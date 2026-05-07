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
.app-error { background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.35); border-radius: 14px; color: #fecaca; padding: 1rem; }
.app-error strong { display: block; margin-bottom: 0.35rem; color: #fff; }
</style>
