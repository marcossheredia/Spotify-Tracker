<template>
  <div class="app-layout">
    <AppNavbar />
    <main class="app-content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import AppNavbar from "@/shared/components/AppNavbar.vue";
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: var(--color-bg, #121212);
}

.app-content {
  min-height: calc(100vh - 64px);
}
</style>
