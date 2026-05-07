<template>
  <div class="assistant-view">
    <header class="view-header">
      <div>
        <h1>Asistente de playlists</h1>
        <p>Escribe lo que quieres y creare una playlist real en tu Spotify.</p>
      </div>
      <div class="status-badges">
        <span class="status-badge" :class="`status-badge--${phase}`">
          {{ statusLabel }}
        </span>
        <button
          v-if="ttsSupported"
          type="button"
          class="tts-toggle"
          :class="{ active: ttsEnabled }"
          @click="toggleTts"
        >
          Voz: {{ ttsEnabled ? "On" : "Off" }}
        </button>
      </div>
    </header>

    <section class="assistant-layout">
      <div class="assistant-side">
        <AssistantAvatar
          :state="isLoading ? 'generating' : 'idle'"
          name="Asistente"
          subtitle="Dime que quieres escuchar y preparare una playlist para ti."
        />
      </div>

      <div class="assistant-main">
        <div class="chat-panel">
          <div class="chat-history">
            <div
              v-for="message in chatMessages"
              :key="message.id"
              class="chat-message"
              :class="`chat-message--${message.role}`"
            >
              <p class="chat-text">{{ message.content }}</p>

              <div v-if="message.result" class="result-card">
                <div class="result-header">
                  <div>
                    <h3>{{ message.result.playlistName || "Playlist creada" }}</h3>
                    <p>{{ message.result.tracksAdded }} canciones agregadas</p>
                  </div>
                  <a
                    v-if="message.result.externalUrl"
                    class="result-link"
                    :href="message.result.externalUrl"
                    target="_blank"
                    rel="noreferrer"
                  >
                    Abrir en Spotify
                  </a>
                </div>

                <ul v-if="message.result.tracks.length" class="tracks-list">
                  <li v-for="track in message.result.tracks.slice(0, 10)" :key="track.id">
                    <span class="track-name">{{ track.name }}</span>
                    <span class="track-artist">{{ track.artist }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <p v-if="error" class="error-banner">{{ error }}</p>

          <form class="chat-composer" @submit.prevent="handleSubmit">
            <textarea
              v-model="messageInput"
              rows="3"
              placeholder="Ej: Creame una playlist de rock alternativo para entrenar"
              :disabled="isLoading"
            />
            <div class="composer-actions">
              <div class="options">
                <label class="option-item">
                  <span>Publica</span>
                  <input v-model="publicPlaylist" type="checkbox" :disabled="isLoading" />
                </label>
                <label class="option-item">
                  <span>Canciones</span>
                  <input
                    v-model.number="trackLimit"
                    type="number"
                    min="5"
                    max="50"
                    step="1"
                    :disabled="isLoading"
                  />
                </label>
              </div>
              <button type="submit" class="send-btn" :disabled="isLoading || !canSend">
                {{ isLoading ? "Creando..." : "Enviar" }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import AssistantAvatar from "@/modules/assistant/components/AssistantAvatar.vue";
import { useAssistant } from "@/modules/assistant/composables/useAssistant";

const messageInput = ref("");
const publicPlaylist = ref(false);
const trackLimit = ref(25);
const chatMessages = ref([
  {
    id: "welcome",
    role: "assistant",
    content: "Hola. Dime el tipo de playlist que quieres crear.",
  },
]);

const ttsEnabled = ref(false);
const ttsSupported = typeof window !== "undefined" && "speechSynthesis" in window;

const { phase, isLoading, error, createPlaylist } = useAssistant();

const statusLabel = computed(() => {
  switch (phase.value) {
    case "thinking":
      return "Pensando";
    case "creating":
      return "Creando";
    case "success":
      return "Listo";
    case "error":
      return "Error";
    default:
      return "En espera";
  }
});

const canSend = computed(() => messageInput.value.trim().length > 0);

function handleSubmit() {
  sendMessage();
}

async function sendMessage() {
  if (!canSend.value || isLoading.value) {
    return;
  }

  const messageText = messageInput.value.trim();
  const userMessage = {
    id: buildId(),
    role: "user",
    content: messageText,
  };

  chatMessages.value = [...chatMessages.value, userMessage];
  messageInput.value = "";

  const result = await createPlaylist(messageText, {
    publicPlaylist: publicPlaylist.value,
    trackLimit: trackLimit.value,
  });

  if (result) {
    const assistantMessage = {
      id: buildId(),
      role: "assistant",
      content: result.message || "Playlist creada correctamente.",
      result,
    };
    chatMessages.value = [...chatMessages.value, assistantMessage];

    if (ttsSupported && ttsEnabled.value) {
      speak(result.message || "Playlist creada correctamente.");
    }
  } else if (error.value) {
    chatMessages.value = [
      ...chatMessages.value,
      {
        id: buildId(),
        role: "assistant",
        content: error.value,
      },
    ];
  }
}

function toggleTts() {
  ttsEnabled.value = !ttsEnabled.value;
  if (!ttsEnabled.value) {
    stopSpeak();
  }
}

function speak(text) {
  if (!ttsSupported || !text || !ttsEnabled.value) {
    return;
  }

  stopSpeak();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "es-ES";
  window.speechSynthesis.speak(utterance);
}

function stopSpeak() {
  if (!ttsSupported) {
    return;
  }

  window.speechSynthesis.cancel();
}

function buildId() {
  return `msg_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}
</script>

<style scoped>
.assistant-view {
  padding: 0;
  color: var(--color-text);
  min-height: calc(100vh - 64px);
}

.view-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.view-header h1 {
  margin: 0 0 0.4rem;
  font-size: 2rem;
  color: var(--color-text);
  font-weight: 700;
}

.view-header p {
  margin: 0;
  color: var(--color-muted);
}

.status-badges {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.assistant-layout {
  display: grid;
  grid-template-columns: minmax(280px, 330px) minmax(0, 1fr);
  gap: 1.8rem;
  align-items: start;
}

.assistant-side {
  position: sticky;
  top: 1.5rem;
}

.assistant-main {
  min-width: 0;
}

.status-badge {
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  background: rgba(207, 163, 113, 0.16);
  color: var(--color-primary);
  border: 1px solid rgba(207, 163, 113, 0.35);
  font-size: 0.85rem;
}

.status-badge--thinking {
  background: rgba(185, 133, 73, 0.18);
  color: var(--color-warning);
  border-color: rgba(185, 133, 73, 0.35);
}

.status-badge--creating {
  background: rgba(18, 50, 55, 0.12);
  color: var(--color-primary);
  border-color: rgba(18, 50, 55, 0.3);
}

.status-badge--success {
  background: rgba(61, 107, 87, 0.15);
  color: var(--color-success);
  border-color: rgba(61, 107, 87, 0.35);
}

.status-badge--error {
  background: rgba(94, 33, 40, 0.12);
  color: var(--color-accent-wine);
  border-color: rgba(94, 33, 40, 0.35);
}

.tts-toggle {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  border-radius: 999px;
  padding: 0.35rem 0.75rem;
  cursor: pointer;
  font-size: 0.8rem;
  transition: border-color 0.18s;
}

.tts-toggle.active {
  border-color: var(--color-accent);
  color: var(--color-primary);
}

.chat-panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  padding: 1.5rem;
  box-shadow: var(--app-shadow-card);
}

.chat-history {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 55vh;
  overflow-y: auto;
  padding-right: 0.5rem;
}

.chat-message {
  max-width: 90%;
  padding: 0.85rem 1rem;
  border-radius: 16px;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border-soft);
  color: var(--color-text);
}

.chat-message--user {
  margin-left: auto;
  background:
    radial-gradient(circle at top right, rgba(207, 163, 113, 0.18), transparent 40%),
    linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  color: var(--color-text-inverse);
  border-color: rgba(233, 220, 186, 0.2);
}

.chat-text {
  margin: 0;
  line-height: 1.5;
}

.result-card {
  margin-top: 0.85rem;
  padding: 0.85rem;
  background: var(--color-bg);
  border-radius: 12px;
  border: 1px solid var(--color-border);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.result-header h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--color-text);
}

.result-header p {
  margin: 0.25rem 0 0;
  color: var(--color-muted);
  font-size: 0.85rem;
}

.result-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.85rem;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 0.25rem 0.65rem;
  transition: background 0.15s;
}

.result-link:hover {
  background: var(--color-surface-soft);
}

.tracks-list {
  margin: 0.85rem 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 0.35rem;
}

.tracks-list li {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--color-text);
}

.track-name {
  font-weight: 600;
}

.track-artist {
  color: var(--color-muted);
}

.error-banner {
  margin: 1rem 0 0;
  padding: 0.75rem 1rem;
  background: rgba(94, 33, 40, 0.10);
  border: 1px solid rgba(94, 33, 40, 0.35);
  border-radius: 10px;
  color: var(--color-accent-wine);
}

.chat-composer {
  margin-top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.chat-composer textarea {
  width: 100%;
  resize: vertical;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  padding: 0.75rem;
  font-family: inherit;
  transition: border-color 0.18s, box-shadow 0.18s;
}

.chat-composer textarea:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(207, 163, 113, 0.18);
}

.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.options {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--color-muted);
}

.option-item input[type="number"] {
  width: 70px;
  padding: 0.3rem 0.4rem;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
}

.send-btn {
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border: 1px solid rgba(233, 220, 186, 0.35);
  padding: 0.6rem 1.4rem;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.18s;
}

.send-btn:hover:not(:disabled) {
  background: var(--color-primary-strong);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .assistant-view {
    padding: 0;
  }

  .view-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .assistant-layout {
    grid-template-columns: 1fr;
  }

  .assistant-side {
    position: static;
  }

  .chat-message {
    max-width: 100%;
  }
}
</style>
