<template>
  <section class="profile-view">
    <h1>Perfil Spotify</h1>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading" class="loading">Cargando perfil...</p>

    <article v-else-if="profile" class="profile-card">
      <img
        v-if="profile.imageUrl"
        :src="profile.imageUrl"
        alt="Avatar Spotify"
        class="avatar"
      />
      <div class="profile-data">
        <h2>{{ profile.displayName || "Usuario Spotify" }}</h2>
        <p>{{ profile.email || "Email no disponible" }}</p>
        <p>Pais: {{ profile.country || "No disponible" }}</p>
        <p>Cuenta: {{ profile.product || "No disponible" }}</p>
        <p>Seguidores: {{ profile.followersTotal ?? "No disponible" }}</p>
        <a
          v-if="profile.externalUrl"
          :href="profile.externalUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="spotify-link"
        >
          Abrir perfil en Spotify
        </a>
        <p class="note">{{ profile.capabilitiesNote }}</p>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted } from "vue";
import { useProfile } from "@/modules/profile/composables/useProfile";

const { profile, loading, error, loadProfile } = useProfile();

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.profile-view {
  min-height: calc(100vh - 64px);
  padding: 2rem;
  background: #121212;
  color: #fff;
}

.profile-card {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
  background: #1c1c1c;
  border: 1px solid #303030;
  border-radius: 12px;
  padding: 1rem;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-data p {
  margin: 0.3rem 0;
  color: #c6c6c6;
}

.spotify-link {
  margin-top: 0.5rem;
  display: inline-block;
  color: #1db954;
}

.note {
  margin-top: 0.7rem;
  font-size: 0.88rem;
  color: #9ea09f;
}

.error { color: #ffb4b4; }
.loading { color: #c8c8c8; }
</style>
