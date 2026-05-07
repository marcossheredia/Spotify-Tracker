<template>
  <section class="app-card">
    <slot />
  </section>
</template>

<style scoped>
.app-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--app-radius-lg); box-shadow: var(--app-shadow-card); padding: 1.25rem; }
</style>
