<template>
  <section class="automation-panel">
    <div class="automation-hero">
      <div>
        <h2>Playlist viva</h2>
        <p>Guarda reglas reutilizables para crear playlists automaticas con tus top canciones.</p>
      </div>
    </div>

    <div class="automation-grid">
      <form class="automation-form" @submit.prevent="handleExecute">
        <label class="automation-field">
          Nombre de la regla
          <input v-model.trim="form.ruleName" type="text" placeholder="Top semanal" />
        </label>

        <label class="automation-field">
          Nombre de la playlist
          <input v-model.trim="form.playlistName" type="text" placeholder="Top del mes" />
        </label>

        <label class="automation-field">
          Descripcion
          <input v-model.trim="form.description" type="text" placeholder="Generada automaticamente" />
        </label>

        <label class="automation-field">
          Periodo
          <select v-model="form.timeRange">
            <option value="short_term">Ultimo mes</option>
            <option value="medium_term">Ultimos 6 meses</option>
            <option value="long_term">Historico</option>
          </select>
        </label>

        <label class="automation-field">
          Numero de canciones
          <input v-model.number="form.limit" type="number" min="5" max="50" />
        </label>

        <label class="automation-checkbox">
          <input v-model="form.publicPlaylist" type="checkbox" />
          Playlist publica
        </label>

        <div class="automation-actions">
          <button
            type="button"
            class="automation-button secondary"
            :disabled="!canSaveRule"
            @click="saveRule"
          >
            Guardar regla
          </button>
          <button
            type="submit"
            class="automation-button"
            :disabled="loading || !canExecute"
          >
            Ejecutar ahora
          </button>
        </div>
      </form>

      <aside class="rules-panel">
        <h3>Reglas guardadas</h3>
        <p v-if="!rules.length" class="empty-rules">No hay reglas guardadas.</p>

        <div v-else class="rules-list">
          <article v-for="rule in rules" :key="rule.id" class="rule-card">
            <div>
              <p class="rule-title">{{ rule.ruleName }}</p>
              <p class="rule-meta">
                {{ formatTimeRange(rule.timeRange) }} · {{ rule.limit }} canciones ·
                {{ rule.publicPlaylist ? "Publica" : "Privada" }}
              </p>
            </div>
            <div class="rule-actions">
              <button type="button" class="automation-button secondary" @click="useRule(rule)">
                Usar
              </button>
              <button
                type="button"
                class="automation-button"
                :disabled="loading"
                @click="executeRule(rule)"
              >
                Ejecutar
              </button>
              <button type="button" class="automation-button secondary" @click="removeRule(rule)">
                Eliminar
              </button>
            </div>
          </article>
        </div>
      </aside>
    </div>

    <article v-if="result" class="automation-result">
      <h4>Playlist creada</h4>
      <p>{{ result.playlistName }} · {{ result.tracksAdded }} canciones</p>
      <a
        v-if="result.externalUrl"
        :href="result.externalUrl"
        target="_blank"
        rel="noopener noreferrer"
      >
        Abrir en Spotify
      </a>
    </article>

    <p v-if="error" class="automation-error">{{ error }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useAutomation } from "@/modules/automation/composables/useAutomation";

const RULES_KEY = "spotify-tracker.assistant.automationRules";

const { loading, error, result, createTopTracksPlaylist } = useAutomation();

const form = reactive({
  ruleName: "",
  playlistName: "",
  description: "",
  timeRange: "short_term",
  limit: 20,
  publicPlaylist: false,
});

const rules = ref([]);
const activeRuleId = ref(null);

const canSaveRule = computed(() => form.ruleName.trim().length > 0);
const canExecute = computed(() => form.playlistName.trim().length > 0 && isLimitValid());

onMounted(() => {
  rules.value = loadRules();
});

function isLimitValid() {
  const value = Number(form.limit || 0);
  return value >= 5 && value <= 50;
}

function loadRules() {
  try {
    const raw = localStorage.getItem(RULES_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function persistRules() {
  localStorage.setItem(RULES_KEY, JSON.stringify(rules.value));
}

function saveRule() {
  if (!canSaveRule.value) {
    return;
  }

  const payload = buildRulePayload();
  if (!payload) {
    return;
  }

  if (activeRuleId.value) {
    rules.value = rules.value.map((rule) => (rule.id === activeRuleId.value ? payload : rule));
  } else {
    rules.value = [payload, ...rules.value];
  }

  persistRules();
}

function useRule(rule) {
  activeRuleId.value = rule.id;
  form.ruleName = rule.ruleName;
  form.playlistName = rule.playlistName;
  form.description = rule.description || "";
  form.timeRange = rule.timeRange;
  form.limit = rule.limit;
  form.publicPlaylist = rule.publicPlaylist;
}

function removeRule(rule) {
  rules.value = rules.value.filter((item) => item.id !== rule.id);
  if (activeRuleId.value === rule.id) {
    activeRuleId.value = null;
  }
  persistRules();
}

async function executeRule(rule) {
  const payload = buildPayloadFromRule(rule);
  if (!payload) {
    return;
  }

  await createTopTracksPlaylist(payload);
}

async function handleExecute() {
  if (!canExecute.value) {
    return;
  }

  const payload = buildPayloadFromForm();
  if (!payload) {
    return;
  }

  await createTopTracksPlaylist(payload);
}

function buildPayloadFromForm() {
  return buildPayload({
    name: form.playlistName,
    description: form.description,
    timeRange: form.timeRange,
    limit: form.limit,
    publicPlaylist: form.publicPlaylist,
  });
}

function buildPayloadFromRule(rule) {
  return buildPayload({
    name: rule.playlistName,
    description: rule.description,
    timeRange: rule.timeRange,
    limit: rule.limit,
    publicPlaylist: rule.publicPlaylist,
  });
}

function buildPayload(payload) {
  const limit = Number(payload.limit || 0);
  if (!payload.name || !payload.name.trim() || limit < 5 || limit > 50) {
    return null;
  }

  return {
    name: payload.name.trim(),
    description: payload.description || "",
    timeRange: payload.timeRange,
    limit,
    publicPlaylist: !!payload.publicPlaylist,
  };
}

function buildRulePayload() {
  const limit = Number(form.limit || 0);
  if (!form.ruleName.trim() || limit < 5 || limit > 50) {
    return null;
  }

  return {
    id: activeRuleId.value || buildId(),
    ruleName: form.ruleName.trim(),
    playlistName: form.playlistName.trim(),
    description: form.description.trim(),
    timeRange: form.timeRange,
    limit,
    publicPlaylist: !!form.publicPlaylist,
  };
}

function formatTimeRange(range) {
  switch (range) {
    case "short_term":
      return "Ultimo mes";
    case "medium_term":
      return "Ultimos 6 meses";
    case "long_term":
      return "Historico";
    default:
      return "No disponible";
  }
}

function buildId() {
  return `rule_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}
</script>

<style scoped>
.automation-panel {
  display: grid;
  gap: 1.5rem;
  color: var(--color-text);
}

.automation-hero {
  background: linear-gradient(135deg, rgba(18, 50, 55, 0.12), rgba(18, 50, 55, 0.04));
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  padding: 1.25rem;
  box-shadow: var(--app-shadow-card);
}

.automation-hero h2 {
  margin: 0 0 0.35rem;
  font-size: 1.3rem;
  color: var(--color-text);
}

.automation-hero p {
  margin: 0;
  color: var(--color-muted);
}

.automation-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: 1.2rem;
  align-items: start;
}

.automation-form {
  display: grid;
  gap: 0.8rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  padding: 1.1rem;
  box-shadow: var(--app-shadow-card);
}

.automation-field {
  display: grid;
  gap: 0.35rem;
  color: var(--color-muted);
  font-size: 0.85rem;
}

.automation-field input,
.automation-field select {
  border: 1px solid var(--color-border-soft);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 10px;
  padding: 0.5rem 0.65rem;
  font-family: inherit;
}

.automation-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--color-muted);
}

.automation-actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.automation-button {
  border: none;
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border-radius: 999px;
  padding: 0.5rem 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s;
}

.automation-button.secondary {
  background: var(--color-surface-soft);
  color: var(--color-text);
  border: 1px solid var(--color-border-soft);
}

.automation-button:hover:not(:disabled) {
  background: var(--color-primary-strong);
  transform: translateY(-1px);
}

.automation-button.secondary:hover:not(:disabled) {
  border-color: var(--color-accent);
  background: var(--color-surface);
}

.automation-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.rules-panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  padding: 1rem;
  box-shadow: var(--app-shadow-card);
  display: grid;
  gap: 0.85rem;
}

.rules-panel h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--color-text);
}

.rules-list {
  display: grid;
  gap: 0.75rem;
}

.rule-card {
  border: 1px solid var(--color-border-soft);
  background: var(--color-surface-soft);
  border-radius: 12px;
  padding: 0.75rem;
  display: grid;
  gap: 0.6rem;
}

.rule-title {
  margin: 0;
  font-weight: 700;
  color: var(--color-text);
}

.rule-meta {
  margin: 0.2rem 0 0;
  color: var(--color-muted);
  font-size: 0.8rem;
}

.rule-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.automation-result {
  background: rgba(61, 107, 87, 0.1);
  border: 1px solid rgba(61, 107, 87, 0.35);
  border-radius: 12px;
  padding: 0.85rem 1rem;
  color: var(--color-text);
}

.automation-result h4 {
  margin: 0 0 0.35rem;
  color: var(--color-success);
}

.automation-result a {
  color: var(--color-success);
  font-weight: 600;
  text-decoration: none;
}

.automation-error {
  color: var(--color-accent-wine);
  font-size: 0.9rem;
}

.empty-rules {
  color: var(--color-muted);
  font-size: 0.85rem;
}

@media (max-width: 900px) {
  .automation-grid {
    grid-template-columns: 1fr;
  }
}
</style>
