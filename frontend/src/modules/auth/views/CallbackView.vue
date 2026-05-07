<template>
  <div class="callback-page">
    <div class="callback-card">
      <template v-if="loading">
        <div class="spinner"></div>
        <p>Iniciando sesión con Spotify…</p>
      </template>
      <template v-else-if="error">
        <p class="error-msg">{{ error }}</p>
        <RouterLink to="/" class="btn-home">Volver al inicio</RouterLink>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "@/stores/authStore";

const router   = useRouter();
const route    = useRoute();
const authStore = useAuthStore();

const loading = ref(true);
const error   = ref(null);

onMounted(async () => {
  const token = route.query.token;

  if (!token) {
    error.value = "No se recibió el token de autenticación.";
    loading.value = false;
    return;
  }

  authStore.setToken(token);
  await authStore.fetchCurrentUser();

  if (authStore.isAuthenticated) {
    router.replace({ name: "Dashboard" });
  } else {
    error.value = "Error al obtener los datos del usuario.";
    loading.value = false;
  }
});
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.callback-card {
  text-align: center;
  color: var(--color-accent-soft);
  font-size: 1.1rem;
  padding: 2rem;
  background: rgba(253, 248, 239, 0.10);
  border: 1px solid rgba(233, 220, 186, 0.25);
  border-radius: var(--app-radius-lg);
  backdrop-filter: blur(8px);
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(207, 163, 113, 0.3);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1.5rem;
}

@keyframes spin { to { transform: rotate(360deg); } }

.error-msg { color: #f4a4a4; margin-bottom: 1rem; }

.btn-home {
  display: inline-block;
  padding: 0.6rem 1.4rem;
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border-radius: 500px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid rgba(233, 220, 186, 0.35);
  transition: opacity 0.2s;
}

.btn-home:hover { opacity: 0.85; }
</style>
