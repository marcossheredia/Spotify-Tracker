<template>
  <section class="automation-panel">
    <header class="automation-hero">
      <h2>Playlist automática</h2>
      <p>Crea una playlist con tus canciones más escuchadas según el periodo seleccionado.</p>
    </header>

    <form class="automation-form" @submit.prevent="handleSubmit">
      <div class="automation-field">
        <label for="playlistName">Nombre de la playlist</label>
        <input id="playlistName" v-model.trim="form.name" type="text" />
      </div>

      <div class="automation-field">
        <label for="timeRange">Periodo</label>
        <select id="timeRange" v-model="form.timeRange" @change="syncDefaultName">
          <option value="short_term">Últimas 4 semanas</option>
          <option value="medium_term">Últimos 6 meses</option>
          <option value="long_term">Último año</option>
        </select>
      </div>

      <div class="automation-field">
        <label for="limit">Número de canciones</label>
        <select id="limit" v-model.number="form.limit">
          <option :value="10">10</option>
          <option :value="25">25</option>
          <option :value="50">50</option>
        </select>
      </div>

      <label class="automation-checkbox">
        <input v-model="form.publicPlaylist" type="checkbox" />
        <span>Playlist pública</span>
      </label>

      <div class="automation-actions">
        <button type="submit" class="automation-button" :disabled="loading">
          {{ loading ? "Creando..." : "Crear playlist" }}
        </button>
      </div>

      <p v-if="error" class="automation-error">{{ error }}</p>
    </form>

    <article v-if="result" class="automation-result">
      <h3>Playlist creada</h3>
      <p><strong>{{ result.playlistName || form.name }}</strong></p>
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
import { reactive } from "vue";
import { useAutomation } from "@/modules/automation/composables/useAutomation";

const form = reactive({
  name: "Mis top canciones - Últimos 6 meses",
  timeRange: "medium_term",
  limit: 25,
  publicPlaylist: false,
});

const { loading, error, result, createTopTracksPlaylist } = useAutomation();

function defaultNameForRange(timeRange) {
  if (timeRange === "short_term") return "Mis top canciones - Últimas 4 semanas";
  if (timeRange === "long_term") return "Mis top canciones - Último año";
  return "Mis top canciones - Últimos 6 meses";
}

function syncDefaultName() {
  form.name = defaultNameForRange(form.timeRange);
}

async function handleSubmit() {
  if (![10, 25, 50].includes(Number(form.limit))) {
    form.limit = 25;
  }

  await createTopTracksPlaylist({
    name: form.name || defaultNameForRange(form.timeRange),
    timeRange: form.timeRange,
    limit: form.limit,
    publicPlaylist: !!form.publicPlaylist,
  });
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

.automation-form {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  box-shadow: var(--app-shadow-card);
  padding: 1rem;
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

.automation-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.automation-result {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-lg);
  box-shadow: var(--app-shadow-card);
  padding: 1rem 1.2rem;
}

.automation-result h3 {
  margin: 0 0 0.35rem;
  color: var(--color-text);
}

.automation-result p {
  margin: 0.25rem 0;
  color: var(--color-muted);
}

.automation-result a {
  color: var(--color-primary);
  font-weight: 600;
}

.automation-error {
  margin: 0;
  padding: 0.55rem 0.75rem;
  border: 1px solid color-mix(in srgb, var(--color-danger) 40%, transparent);
  border-radius: 10px;
  color: var(--color-danger);
  background: color-mix(in srgb, var(--color-danger) 12%, transparent);
  font-size: 0.9rem;
}
</style>
