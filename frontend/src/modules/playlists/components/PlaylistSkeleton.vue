<template>
  <section v-if="mode === 'grid'" class="skeleton-grid">
    <article v-for="index in safeCount" :key="`grid-${index}`" class="skeleton-card">
      <div class="skeleton-cover"></div>
      <div class="skeleton-line line-1"></div>
      <div class="skeleton-line line-2"></div>
      <div class="skeleton-line line-3"></div>
    </article>
  </section>

  <section v-else class="skeleton-list">
    <article v-for="index in safeCount" :key="`list-${index}`" class="skeleton-row">
      <div class="skeleton-thumb"></div>
      <div class="skeleton-texts">
        <div class="skeleton-line line-1"></div>
        <div class="skeleton-line line-2"></div>
      </div>
      <div class="skeleton-chip"></div>
    </article>
  </section>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  mode: {
    type: String,
    default: "list",
  },
  count: {
    type: Number,
    default: 8,
  },
});

const safeCount = computed(() => Math.max(1, Math.min(Number(props.count) || 8, 20)));
</script>

<style scoped>
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.9rem;
}

.skeleton-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 0.75rem;
}

.skeleton-cover {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  margin-bottom: 0.7rem;
  background: linear-gradient(90deg, var(--color-surface-soft), var(--color-surface-strong), var(--color-surface-soft));
  background-size: 200% 100%;
  animation: shimmer 1.2s linear infinite;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.skeleton-row {
  display: grid;
  grid-template-columns: 52px 1fr auto;
  gap: 0.75rem;
  align-items: center;
  border-radius: 10px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  padding: 0.6rem;
}

.skeleton-thumb {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  background: linear-gradient(90deg, var(--color-surface-soft), var(--color-surface-strong), var(--color-surface-soft));
  background-size: 200% 100%;
  animation: shimmer 1.2s linear infinite;
}

.skeleton-texts {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.skeleton-line {
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--color-surface-soft), var(--color-surface-strong), var(--color-surface-soft));
  background-size: 200% 100%;
  animation: shimmer 1.2s linear infinite;
}

.line-1 {
  width: 65%;
}

.line-2 {
  width: 45%;
}

.line-3 {
  width: 35%;
  margin-top: 0.45rem;
}

.skeleton-chip {
  width: 72px;
  height: 24px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--color-surface-soft), var(--color-surface-strong), var(--color-surface-soft));
  background-size: 200% 100%;
  animation: shimmer 1.2s linear infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}
</style>
