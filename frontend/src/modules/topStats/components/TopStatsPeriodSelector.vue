<template>
  <div class="period-selector">
    <label for="top-stats-period">Periodo Top</label>
    <select
      id="top-stats-period"
      :value="modelValue"
      @change="onChange"
    >
      <option
        v-for="option in periodOptions"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label }}
      </option>
    </select>
  </div>
</template>

<script setup>
import { TopStatsPeriod } from "../domain/TopStatsPeriod";

const props = defineProps({
  modelValue: {
    type: String,
    default: TopStatsPeriod.SHORT_TERM,
  },
});

const emit = defineEmits(["update:modelValue"]);
const periodOptions = TopStatsPeriod.options();

function onChange(event) {
  const nextValue = TopStatsPeriod.normalize(event.target.value);
  if (nextValue !== props.modelValue) {
    emit("update:modelValue", nextValue);
  }
}
</script>

<style scoped>
.period-selector {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  margin-bottom: 1rem;
}

.period-selector label {
  color: var(--color-muted);
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.period-selector select {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  border-radius: 8px;
  padding: 0.45rem 0.7rem;
  font-size: 0.9rem;
  transition: border-color 0.18s;
}

.period-selector select:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(207, 163, 113, 0.18);
}
</style>
