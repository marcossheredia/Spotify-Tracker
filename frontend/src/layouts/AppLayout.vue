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
  background:
    radial-gradient(circle at bottom left, rgba(207, 163, 113, 0.18), transparent 30%),
    var(--color-bg);
}

.app-content {
  min-height: calc(100vh - 64px);
  padding: var(--app-page-padding);
}
</style>
