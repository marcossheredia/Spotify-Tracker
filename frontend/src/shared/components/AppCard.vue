<template>
  <section class="app-card">
    <slot />
  </section>
</template>

<style scoped>
.app-card { background: linear-gradient(180deg, rgba(40, 40, 40, 0.98), rgba(24, 24, 24, 0.98)); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: var(--app-radius-lg, 22px); box-shadow: var(--app-shadow-card, 0 18px 45px rgba(0, 0, 0, 0.25)); padding: 1.25rem; }
</style>
