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
  background: #282828;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #383838;
}

.placeholder-section h3 {
  margin-bottom: 0.6rem;
  font-size: 1.1rem;
}

.placeholder-section p {
  color: #b3b3b3;
  font-size: 0.9rem;
}
</style>