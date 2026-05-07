<template>
  <div class="placeholder-section">
    <h3>{{ title }}</h3>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  message: {
    type: String,
    required: true,
  },
});
</script>

<style scoped>
.placeholder-section {
  background: var(--color-surface);
  border-radius: var(--app-radius-md);
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  box-shadow: var(--app-shadow-soft);
}

.placeholder-section h3 {
  margin-bottom: 0.6rem;
  font-size: 1.1rem;
  color: var(--color-text);
}

.placeholder-section p {
  color: var(--color-muted);
  font-size: 0.9rem;
}
</style>