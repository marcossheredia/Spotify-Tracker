<template>
  <Teleport to="body">
    <div v-if="modelValue" class="app-modal-backdrop" @click.self="$emit('update:modelValue', false)">
      <section class="app-modal" role="dialog" aria-modal="true">
        <header class="app-modal__header">
          <h2>{{ title }}</h2>
          <button type="button" @click="$emit('update:modelValue', false)">×</button>
        </header>
        <slot />
      </section>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({ modelValue: { type: Boolean, required: true }, title: { type: String, default: "Detalle" } });
defineEmits(["update:modelValue"]);
</script>

<style scoped>
.app-modal-backdrop { position: fixed; inset: 0; z-index: 500; display: grid; place-items: center; background: rgba(18, 50, 55, 0.55); padding: 1rem; }
.app-modal { width: min(680px, 100%); max-height: 90vh; overflow: auto; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 22px; padding: 1.25rem; box-shadow: var(--app-shadow-card); }
.app-modal__header { display: flex; justify-content: space-between; gap: 1rem; align-items: center; margin-bottom: 1rem; }
.app-modal__header button { width: 34px; height: 34px; border-radius: 50%; border: 1px solid var(--color-border); background: var(--color-surface-soft); color: var(--color-text); cursor: pointer; transition: background 0.15s; }
.app-modal__header button:hover { background: var(--color-surface-strong); }
</style>
