<template>
  <aside class="assistant-avatar-card" :class="stateClass">
    <div class="assistant-avatar-image-wrapper">
      <img class="assistant-avatar-image" :src="currentImage" :alt="altText" />
    </div>

    <div class="assistant-avatar-info">
      <div class="assistant-avatar-header">
        <h2 class="assistant-avatar-name">{{ name }}</h2>
        <span class="assistant-avatar-status" :class="statusClass">
          {{ statusLabel }}
        </span>
      </div>
      <p class="assistant-avatar-description">{{ description }}</p>
    </div>
  </aside>
</template>

<script setup>
import { computed } from "vue";
import idleImage from "@/assets/PersonajeEspera.jpeg";
import generatingImage from "@/assets/PersonajeGenerando.jpeg";

const props = defineProps({
  state: {
    type: String,
    default: "idle",
    validator: (value) => ["idle", "generating"].includes(value),
  },
  name: {
    type: String,
    default: "Asistente",
  },
  subtitle: {
    type: String,
    default: "Tu creador inteligente de playlists.",
  },
});

const isGenerating = computed(() => props.state === "generating");

const currentImage = computed(() =>
  isGenerating.value ? generatingImage : idleImage
);

const statusLabel = computed(() =>
  isGenerating.value ? "Generando" : "En espera"
);

const description = computed(() =>
  isGenerating.value ? "Estoy preparando tu playlist..." : props.subtitle
);

const statusClass = computed(() =>
  isGenerating.value ? "is-generating" : "is-idle"
);

const stateClass = computed(() =>
  isGenerating.value ? "state-generating" : "state-idle"
);

const altText = computed(() =>
  isGenerating.value
    ? "Asistente generando una playlist"
    : "Asistente esperando instrucciones"
);
</script>

<style scoped>
.assistant-avatar-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: var(--app-shadow-card);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.assistant-avatar-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 5;
  border-radius: 18px;
  overflow: hidden;
  background: var(--color-surface-strong);
}

.assistant-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.assistant-avatar-card.state-generating .assistant-avatar-image {
  transform: scale(1.03);
}

.assistant-avatar-info {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.assistant-avatar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.assistant-avatar-name {
  font-size: 1.2rem;
  margin: 0;
  color: var(--color-text);
  font-weight: 700;
}

.assistant-avatar-status {
  padding: 0.2rem 0.65rem;
  border-radius: 999px;
  font-size: 0.75rem;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.assistant-avatar-status.is-idle {
  background: rgba(61, 107, 87, 0.15);
  color: var(--color-success);
  border: 1px solid rgba(61, 107, 87, 0.3);
}

.assistant-avatar-status.is-generating {
  background: rgba(185, 133, 73, 0.18);
  color: var(--color-warning);
  border: 1px solid rgba(185, 133, 73, 0.35);
}

.assistant-avatar-description {
  margin: 0;
  color: var(--color-muted);
  line-height: 1.5;
}

.assistant-avatar-card.state-generating {
  animation: avatarPulse 2.4s ease-in-out infinite;
}

@keyframes avatarPulse {
  0% {
    box-shadow: var(--app-shadow-card);
  }
  50% {
    box-shadow: 0 20px 55px rgba(207, 163, 113, 0.35);
  }
  100% {
    box-shadow: var(--app-shadow-card);
  }
}
</style>
