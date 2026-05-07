<template>
  <button
    class="app-button"
    :class="[`app-button--${variant}`, { 'app-button--block': block }]"
    :type="type"
    :disabled="disabled || loading"
  >
    <span v-if="loading" class="app-button__spinner" aria-hidden="true"></span>
    <slot />
  </button>
</template>

<script setup>
defineProps({
  type: { type: String, default: "button" },
  variant: { type: String, default: "primary" },
  block: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
});
</script>

<style scoped>
.app-button { display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; border: 0; border-radius: 999px; padding: 0.75rem 1.15rem; font-weight: 700; cursor: pointer; transition: transform 0.18s ease, opacity 0.18s ease, background 0.18s ease; }
.app-button:hover:not(:disabled) { transform: translateY(-1px); }
.app-button:disabled { cursor: not-allowed; opacity: 0.6; }
.app-button--primary { background: var(--color-accent, #1db954); color: #041006; }
.app-button--secondary { background: #282828; color: #fff; border: 1px solid #3d3d3d; }
.app-button--ghost { background: transparent; color: #fff; border: 1px solid #444; }
.app-button--danger { background: #ef4444; color: #fff; }
.app-button--block { width: 100%; }
.app-button__spinner { width: 0.9rem; height: 0.9rem; border: 2px solid currentColor; border-right-color: transparent; border-radius: 50%; animation: spin 0.75s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
