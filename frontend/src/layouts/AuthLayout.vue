<template>
  <main class="auth-layout">
    <slot />
  </main>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(207, 163, 113, 0.22), transparent 38%),
    radial-gradient(circle at bottom left, rgba(233, 220, 186, 0.14), transparent 32%),
    linear-gradient(135deg, #0F2D32 0%, #123237 55%, #163B41 100%);
}
</style>
