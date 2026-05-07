<template>
  <div class="home">
    <div class="hero">
      <div class="hero-content">
        <div class="logo-area">
          <svg class="spotify-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3z"/>
          </svg>
          <h1>Spotify Tracker</h1>
        </div>
        <p class="subtitle">
          Consulta tus playlists, canciones favoritas y toda tu actividad musical en un solo lugar.
        </p>
        <SpotifyLoginButton @click="login" />
      </div>
    </div>
  </div>
</template>

<script setup>
import SpotifyLoginButton from "@/modules/auth/components/SpotifyLoginButton.vue";
import { useAuth } from "@/shared/composables/useAuth";

const { login } = useAuth();
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-content {
  text-align: center;
  padding: 2.5rem;
  max-width: 480px;
  background: rgba(253, 248, 239, 0.10);
  border: 1px solid rgba(233, 220, 186, 0.25);
  border-radius: var(--app-radius-lg);
  backdrop-filter: blur(8px);
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.spotify-icon {
  width: 52px;
  height: 52px;
  color: var(--color-accent);
}

h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-accent-soft);
  letter-spacing: -0.5px;
}

.subtitle {
  color: rgba(233, 220, 186, 0.75);
  font-size: 1.05rem;
  line-height: 1.6;
  margin-bottom: 2.5rem;
}
</style>
