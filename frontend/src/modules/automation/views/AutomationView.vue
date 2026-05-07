<template>
  <section class="automation-view">
    <h1>Automatizacion de Playlists</h1>
    <p class="subtitle">
      Crea una playlist automatica con tus top tracks del periodo seleccionado.
    </p>

    <form class="form" @submit.prevent="submit">
      <label>
        Nombre
        <input v-model="form.name" type="text" placeholder="Top del mes - Spotify Tracker" />
      </label>

      <label>
        Descripcion
        <input v-model="form.description" type="text" placeholder="Generada automaticamente" />
      </label>

      <label>
        Periodo
        <select v-model="form.timeRange">
          <option value="short_term">Ultimo mes</option>
          <option value="medium_term">Ultimos 6 meses</option>
          <option value="long_term">Historico</option>
        </select>
      </label>

      <label>
        Numero de tracks
        <input v-model.number="form.limit" type="number" min="1" max="50" />
      </label>

      <label class="checkbox">
        <input v-model="form.publicPlaylist" type="checkbox" />
        Playlist publica
      </label>

      <button :disabled="loading" type="submit">Crear playlist automatica</button>
    </form>

    <p v-if="error" class="error">{{ error }}</p>

    <article v-if="result" class="result">
      <h2>Playlist creada</h2>
      <p>{{ result.playlistName }} · {{ result.tracksAdded }} tracks</p>
      <a :href="result.externalUrl" target="_blank" rel="noopener noreferrer">Abrir en Spotify</a>
    </article>
  </section>
</template>

<script setup>
import { reactive } from "vue";
import { useAutomation } from "@/modules/automation/composables/useAutomation";

const { loading, error, result, createTopTracksPlaylist } = useAutomation();
const form = reactive({
  name: "Top del mes - Spotify Tracker",
  description: "Playlist creada automaticamente desde Spotify Tracker",
  timeRange: "short_term",
  limit: 20,
  publicPlaylist: false,
});

async function submit() {
  await createTopTracksPlaylist({ ...form });
}
</script>

<style scoped>
.automation-view {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.automation-view h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
}

.subtitle {
  color: var(--color-muted);
  margin-top: 0.4rem;
}

.form {
  margin-top: 1rem;
  display: grid;
  gap: 0.7rem;
  max-width: 540px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-md);
  padding: 1rem;
  box-shadow: var(--app-shadow-soft);
}

label {
  display: grid;
  gap: 0.35rem;
  color: var(--color-muted);
  font-size: 0.9rem;
}

.checkbox {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

input,
select {
  border: 1px solid var(--color-border);
  background: var(--color-surface-soft);
  color: var(--color-text);
  border-radius: 8px;
  padding: 0.45rem 0.6rem;
  font-family: inherit;
  transition: border-color 0.18s, box-shadow 0.18s;
}

input:focus,
select:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(207, 163, 113, 0.18);
}

button[type="submit"] {
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border: 1px solid rgba(233, 220, 186, 0.35);
  border-radius: 8px;
  padding: 0.55rem 0.6rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.18s;
}

button[type="submit"]:hover:not(:disabled) {
  background: var(--color-primary-strong);
}

.result {
  margin-top: 1rem;
  border: 1px solid rgba(61, 107, 87, 0.35);
  background: rgba(61, 107, 87, 0.08);
  border-radius: 10px;
  padding: 0.8rem;
  color: var(--color-text);
}

.result h2 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-success);
  margin-bottom: 0.35rem;
}

.result a {
  color: var(--color-success);
  font-weight: 600;
}

.error {
  margin-top: 0.8rem;
  color: var(--color-accent-wine);
}
</style>
