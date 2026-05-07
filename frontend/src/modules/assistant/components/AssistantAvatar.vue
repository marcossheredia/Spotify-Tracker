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
  background: linear-gradient(160deg, rgba(26, 26, 38, 0.95), rgba(20, 20, 30, 0.9));
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.35);
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
  background: rgba(0, 0, 0, 0.3);
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
}

.assistant-avatar-status {
  padding: 0.2rem 0.65rem;
  border-radius: 999px;
  font-size: 0.75rem;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.assistant-avatar-status.is-idle {
  background: rgba(46, 213, 115, 0.18);
  color: #8ef0b0;
}

.assistant-avatar-status.is-generating {
  background: rgba(90, 190, 255, 0.2);
  color: #a6d9ff;
}

.assistant-avatar-description {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
}

.assistant-avatar-card.state-generating {
  animation: avatarPulse 2.4s ease-in-out infinite;
}

@keyframes avatarPulse {
  0% {
    box-shadow: 0 18px 40px rgba(0, 0, 0, 0.35);
  }
  50% {
    box-shadow: 0 20px 55px rgba(90, 190, 255, 0.35);
  }
  100% {
    box-shadow: 0 18px 40px rgba(0, 0, 0, 0.35);
  }
}
</style>
