<template>
  <main class="auth-layout">
    <slot />
  </main>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  background: radial-gradient(circle at top left, rgba(29, 185, 84, 0.2), transparent 32%),
    linear-gradient(135deg, #050505 0%, #121212 55%, #1d1d1d 100%);
}
</style>
