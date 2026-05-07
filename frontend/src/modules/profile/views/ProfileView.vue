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
  color: var(--color-text);
}

.profile-view h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.profile-card {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-md);
  padding: 1rem;
  box-shadow: var(--app-shadow-soft);
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border);
}

.profile-data h2 {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 0.35rem;
}

.profile-data p {
  margin: 0.3rem 0;
  color: var(--color-muted);
}

.spotify-link {
  margin-top: 0.5rem;
  display: inline-block;
  color: var(--color-primary);
  font-weight: 600;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 0.25rem 0.75rem;
  text-decoration: none;
  transition: border-color 0.15s;
}

.spotify-link:hover {
  border-color: var(--color-accent);
}

.note {
  margin-top: 0.7rem;
  font-size: 0.88rem;
  color: var(--color-muted-soft);
}

.error { color: var(--color-accent-wine); }
.loading { color: var(--color-muted); }
</style>
