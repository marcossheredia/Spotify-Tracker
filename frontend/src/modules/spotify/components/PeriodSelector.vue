<template><div class="period-selector" role="group" aria-label="Seleccionar periodo"><button v-for="period in periods" :key="period.value" type="button" :class="{ active: modelValue === period.value }" @click="$emit('update:modelValue', period.value)">{{ period.label }}</button></div></template>
<script setup>defineProps({ modelValue: { type: String, required: true } }); defineEmits(["update:modelValue"]); const periods = [{ value: "short_term", label: "4 semanas" }, { value: "medium_term", label: "6 meses" }, { value: "long_term", label: "Todo" }];</script>
<style scoped>
.period-selector { display: inline-flex; gap: .35rem; padding: .3rem; border-radius: 999px; background: var(--color-surface-soft); border: 1px solid var(--color-border); }
button { border: 0; border-radius: 999px; background: transparent; color: var(--color-muted); padding: .5rem .8rem; cursor: pointer; font-weight: 700; transition: background 0.18s, color 0.18s; }
button.active { background: var(--color-accent); color: var(--color-primary); }
button:hover:not(.active) { background: rgba(207, 163, 113, 0.18); color: var(--color-primary); }
</style>
