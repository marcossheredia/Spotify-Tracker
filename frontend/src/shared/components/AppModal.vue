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
.app-modal-backdrop { position: fixed; inset: 0; z-index: 500; display: grid; place-items: center; background: rgba(0, 0, 0, 0.62); padding: 1rem; }
.app-modal { width: min(680px, 100%); max-height: 90vh; overflow: auto; background: #181818; border: 1px solid #333; border-radius: 22px; padding: 1.25rem; }
.app-modal__header { display: flex; justify-content: space-between; gap: 1rem; align-items: center; margin-bottom: 1rem; }
.app-modal__header button { width: 34px; height: 34px; border-radius: 50%; border: 1px solid #444; background: #222; color: #fff; cursor: pointer; }
</style>
