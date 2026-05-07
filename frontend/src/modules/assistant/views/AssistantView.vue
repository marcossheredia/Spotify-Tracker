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
  padding: 2rem 2.5rem 3rem;
  color: #fff;
  min-height: calc(100vh - 64px);
  background: linear-gradient(140deg, #0b0b10 0%, #111118 50%, #1b1e2a 100%);
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
}

.view-header p {
  margin: 0;
  color: #b3b3b3;
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
  background: rgba(255, 255, 255, 0.12);
  font-size: 0.85rem;
}

.status-badge--thinking {
  background: rgba(255, 184, 77, 0.2);
  color: #ffcd75;
}

.status-badge--creating {
  background: rgba(90, 190, 255, 0.2);
  color: #8ed2ff;
}

.status-badge--success {
  background: rgba(42, 216, 127, 0.2);
  color: #68e5a7;
}

.status-badge--error {
  background: rgba(255, 92, 92, 0.2);
  color: #ff9b9b;
}

.tts-toggle {
  border: 1px solid #3d3d3d;
  background: #121212;
  color: #fff;
  border-radius: 999px;
  padding: 0.35rem 0.75rem;
  cursor: pointer;
  font-size: 0.8rem;
}

.tts-toggle.active {
  border-color: #1db954;
  color: #1db954;
}

.chat-panel {
  background: rgba(15, 15, 20, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 1.5rem;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.35);
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
  background: rgba(255, 255, 255, 0.05);
}

.chat-message--user {
  margin-left: auto;
  background: rgba(29, 185, 84, 0.2);
}

.chat-text {
  margin: 0;
  line-height: 1.5;
}

.result-card {
  margin-top: 0.85rem;
  padding: 0.85rem;
  background: rgba(12, 12, 18, 0.9);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
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
}

.result-header p {
  margin: 0.25rem 0 0;
  color: #b3b3b3;
  font-size: 0.85rem;
}

.result-link {
  color: #1db954;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.85rem;
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
  color: #d0d0d0;
}

.track-name {
  font-weight: 600;
}

.track-artist {
  color: #9e9e9e;
}

.error-banner {
  margin: 1rem 0 0;
  padding: 0.75rem 1rem;
  background: rgba(255, 80, 80, 0.15);
  border-radius: 10px;
  color: #ffb3b3;
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
  border: 1px solid #2f2f2f;
  background: #0d0d12;
  color: #fff;
  padding: 0.75rem;
  font-family: inherit;
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
  color: #bfbfbf;
}

.option-item input[type="number"] {
  width: 70px;
  padding: 0.3rem 0.4rem;
  border-radius: 8px;
  border: 1px solid #2f2f2f;
  background: #0d0d12;
  color: #fff;
}

.send-btn {
  background: #1db954;
  color: #000;
  border: none;
  padding: 0.6rem 1.4rem;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .assistant-view {
    padding: 1.5rem;
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
