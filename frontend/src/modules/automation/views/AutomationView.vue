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
  background: #121212;
  color: #fff;
  padding: 2rem;
}

.subtitle {
  color: #c0c0c0;
  margin-top: 0.4rem;
}

.form {
  margin-top: 1rem;
  display: grid;
  gap: 0.7rem;
  max-width: 540px;
  background: #1a1a1a;
  border: 1px solid #2f2f2f;
  border-radius: 12px;
  padding: 1rem;
}

label {
  display: grid;
  gap: 0.35rem;
}

.checkbox {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

input,
select,
button {
  border: 1px solid #4b4b4b;
  background: #151515;
  color: #fff;
  border-radius: 8px;
  padding: 0.45rem 0.6rem;
}

.result {
  margin-top: 1rem;
  border: 1px solid #255d37;
  background: #143120;
  border-radius: 10px;
  padding: 0.8rem;
}

.result a {
  color: #9af0a8;
}

.error {
  margin-top: 0.8rem;
  color: #ffb4b4;
}
</style>
