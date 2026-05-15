<template>
  <section class="automation-panel">
    <header class="automation-hero">
      <h2>Playlist viva</h2>
      <p>Guarda reglas reutilizables para crear playlists automáticas con tus top canciones.</p>
    </header>

    <div class="automation-grid">
      <form class="automation-form" @submit.prevent>
        <div class="automation-field">
          <label for="ruleName">Nombre de la regla</label>
          <input id="ruleName" v-model.trim="form.ruleName" type="text" />
        </div>

        <div class="automation-field">
          <label for="playlistName">Nombre de la playlist</label>
          <input id="playlistName" v-model.trim="form.playlistName" type="text" />
        </div>

        <div class="automation-field">
          <label for="description">Descripción</label>
          <textarea id="description" v-model.trim="form.description" rows="3" />
        </div>

        <div class="automation-field">
          <label for="timeRange">Periodo</label>
          <select id="timeRange" v-model="form.timeRange">
            <option value="short_term">Último mes</option>
            <option value="medium_term">Últimos 6 meses</option>
            <option value="long_term">Histórico</option>
          </select>
        </div>

        <div class="automation-field">
          <label for="limit">Número de canciones</label>
          <input id="limit" v-model.number="form.limit" type="number" min="5" max="50" step="1" />
        </div>

        <label class="automation-checkbox">
          <input v-model="form.publicPlaylist" type="checkbox" />
          <span>Playlist pública</span>
        </label>

        <div class="automation-actions">
          <button type="button" class="automation-button secondary" @click="saveRule">Guardar regla</button>
          <button type="button" class="automation-button" :disabled="loading" @click="executeFromForm">
            {{ loading ? "Ejecutando..." : "Ejecutar ahora" }}
          </button>
        </div>

        <p v-if="error" class="automation-error">{{ error }}</p>
      </form>

      <aside class="rules-panel">
        <h3>Reglas guardadas</h3>
        <div v-if="!rules.length" class="empty-rules">Aún no tienes reglas guardadas.</div>
        <div v-else class="rules-list">
          <article v-for="rule in rules" :key="rule.id" class="rule-card">
            <h4>{{ rule.ruleName }}</h4>
            <p class="rule-meta">
              {{ labelTimeRange(rule.timeRange) }} · {{ rule.limit }} canciones ·
              {{ rule.publicPlaylist ? "Pública" : "Privada" }}
            </p>
            <div class="rule-actions">
              <button type="button" class="automation-button secondary" @click="useRule(rule)">Usar</button>
              <button type="button" class="automation-button" :disabled="loading" @click="executeRule(rule)">
                Ejecutar
              </button>
              <button type="button" class="automation-button secondary" @click="deleteRule(rule.id)">Eliminar</button>
            </div>
          </article>
        </div>
      </aside>
    </div>

    <article v-if="result" class="automation-result">
      <h3>Playlist creada</h3>
      <p><strong>{{ result.playlistName || form.playlistName }}</strong></p>
      <p>{{ result.tracksAdded ?? 0 }} canciones añadidas</p>
      <a
        v-if="result.externalUrl"
        :href="result.externalUrl"
        target="_blank"
        rel="noreferrer"
      >
        Abrir en Spotify
      </a>
    </article>
  </section>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import { useAutomation } from "@/modules/automation/composables/useAutomation";

const STORAGE_KEY = "spotify-tracker.assistant.automationRules";

const form = reactive({
  ruleName: "",
  playlistName: "",
  description: "",
  timeRange: "medium_term",
  limit: 25,
  publicPlaylist: false,
});

const savedRules = ref([]);
const { loading, error, result, createTopTracksPlaylist } = useAutomation();

const rules = computed(() => savedRules.value);

onMounted(() => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    savedRules.value = raw ? JSON.parse(raw) : [];
  } catch {
    savedRules.value = [];
  }
});

function persistRules() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(savedRules.value));
}

function normalizeLimit(value) {
  const numeric = Number(value);
  if (Number.isNaN(numeric)) return 25;
  return Math.min(50, Math.max(5, numeric));
}

function buildPayload(source) {
  return {
    name: source.playlistName,
    description: source.description || "",
    timeRange: source.timeRange,
    limit: normalizeLimit(source.limit),
    publicPlaylist: !!source.publicPlaylist,
  };
}

function labelTimeRange(value) {
  if (value === "short_term") return "Último mes";
  if (value === "long_term") return "Histórico";
  return "Últimos 6 meses";
}

function saveRule() {
  if (!form.ruleName.trim()) {
    return;
  }

  const rule = {
    id: `rule_${Date.now()}_${Math.random().toString(16).slice(2)}`,
    ruleName: form.ruleName.trim(),
    playlistName: form.playlistName.trim(),
    description: form.description.trim(),
    timeRange: form.timeRange,
    limit: normalizeLimit(form.limit),
    publicPlaylist: !!form.publicPlaylist,
  };

  savedRules.value = [rule, ...savedRules.value];
  persistRules();
}

function useRule(rule) {
  form.ruleName = rule.ruleName || "";
  form.playlistName = rule.playlistName || "";
  form.description = rule.description || "";
  form.timeRange = rule.timeRange || "medium_term";
  form.limit = normalizeLimit(rule.limit);
  form.publicPlaylist = !!rule.publicPlaylist;
}

async function executeFromForm() {
  form.limit = normalizeLimit(form.limit);
  if (!form.playlistName.trim()) {
    return;
  }

  await createTopTracksPlaylist(buildPayload(form));
}

async function executeRule(rule) {
  if (!rule.playlistName?.trim()) {
    return;
  }

  await createTopTracksPlaylist(buildPayload(rule));
}

function deleteRule(ruleId) {
  savedRules.value = savedRules.value.filter((rule) => rule.id !== ruleId);
  persistRules();
}
</script>

<style scoped>
.automation-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.automation-hero {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  padding: 1rem 1.2rem;
  box-shadow: var(--app-shadow-card);
}

.automation-hero h2 {
  margin: 0 0 0.35rem;
  color: var(--color-text);
}

.automation-hero p {
  margin: 0;
  color: var(--color-muted);
}

.automation-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 1rem;
}

.automation-form,
.rules-panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  box-shadow: var(--app-shadow-card);
  padding: 1rem;
}

.automation-form {
  display: grid;
  gap: 0.8rem;
}

.automation-field {
  display: grid;
  gap: 0.35rem;
}

.automation-field label {
  color: var(--color-muted);
  font-size: 0.9rem;
}

.automation-field input,
.automation-field textarea,
.automation-field select {
  width: 100%;
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 10px;
  padding: 0.65rem 0.75rem;
}

.automation-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-text);
}

.automation-actions {
  display: flex;
  gap: 0.6rem;
}

.automation-button {
  border: 1px solid var(--color-primary-strong);
  background: var(--color-primary);
  color: var(--color-accent);
  border-radius: 999px;
  padding: 0.55rem 0.95rem;
  cursor: pointer;
  font-weight: 600;
}

.automation-button.secondary {
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-color: var(--color-border-soft);
}

.automation-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.rules-panel h3 {
  margin: 0 0 0.75rem;
  color: var(--color-text);
}

.rules-list {
  display: grid;
  gap: 0.7rem;
}

.rule-card {
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border-soft);
  border-radius: 12px;
  padding: 0.7rem;
}

.rule-card h4 {
  margin: 0 0 0.35rem;
  color: var(--color-text);
}

.rule-meta {
  margin: 0 0 0.6rem;
  color: var(--color-muted);
  font-size: 0.88rem;
}

.rule-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.automation-result {
  background: var(--color-surface);
  border: 1px solid color-mix(in srgb, var(--color-success) 45%, var(--color-border));
  border-radius: var(--app-radius-lg);
  padding: 1rem;
}

.automation-result h3 {
  margin: 0 0 0.35rem;
  color: var(--color-success);
}

.automation-result p {
  margin: 0.2rem 0;
  color: var(--color-text);
}

.automation-result a {
  color: var(--color-accent-wine);
  font-weight: 600;
}

.automation-error {
  margin: 0;
  color: var(--color-accent-wine);
}

.empty-rules {
  color: var(--color-muted);
  background: var(--color-surface-soft);
  border: 1px dashed var(--color-border-soft);
  border-radius: 10px;
  padding: 0.8rem;
}

@media (max-width: 960px) {
  .automation-grid {
    grid-template-columns: 1fr;
  }
}
</style>
