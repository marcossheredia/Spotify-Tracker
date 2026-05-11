<template>
  <nav class="navbar">
    <div class="nav-left">
      <div ref="menuRoot" class="nav-menu">
        <button
          type="button"
          class="menu-toggle"
          :aria-expanded="String(menuOpen)"
          aria-controls="main-left-menu"
          @click.stop="toggleMenu"
        >
          <span class="menu-icon">☰</span>
          <span class="menu-label">Menu</span>
        </button>

        <div
          v-if="menuOpen"
          id="main-left-menu"
          class="menu-dropdown"
        >
          <RouterLink to="/profile" class="menu-item" @click="closeMenu">
            Perfil
          </RouterLink>
          <RouterLink to="/library" class="menu-item" @click="closeMenu">
            Biblioteca
          </RouterLink>
          <RouterLink to="/followed-artists" class="menu-item" @click="closeMenu">
            Artistas Seguidos
          </RouterLink>
          <RouterLink to="/search" class="menu-item" @click="closeMenu">
            Busqueda
          </RouterLink>
          <RouterLink to="/player" class="menu-item" @click="closeMenu">
            Player
          </RouterLink>
          <RouterLink to="/automation" class="menu-item" @click="closeMenu">
            Automatizacion
          </RouterLink>
          <RouterLink to="/assistant" class="menu-item" @click="closeMenu">
            Asistente IA
          </RouterLink>
          <RouterLink to="/playtime" class="menu-item" @click="closeMenu">
            Tiempo Escuchado
          </RouterLink>
        </div>
      </div>

      <RouterLink to="/dashboard" class="nav-brand">
        <svg class="brand-icon" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3z"/>
        </svg>
        Spotify Tracker
      </RouterLink>
    </div>

    <div class="nav-user">
      <span class="user-name">{{ user?.displayName }}</span>
      <button class="logout-btn" @click="logout">Cerrar sesión</button>
    </div>
  </nav>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "@/shared/composables/useAuth";

const { logout, authStore } = useAuth();
const user = computed(() => authStore.user);
const route = useRoute();
const menuOpen = ref(false);
const menuRoot = ref(null);

function toggleMenu() {
  menuOpen.value = !menuOpen.value;
}

function closeMenu() {
  menuOpen.value = false;
}

function handleDocumentClick(event) {
  if (!menuOpen.value || !menuRoot.value) {
    return;
  }

  if (!menuRoot.value.contains(event.target)) {
    closeMenu();
  }
}

function handleEscape(event) {
  if (event.key === "Escape") {
    closeMenu();
  }
}

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);
  document.addEventListener("keydown", handleEscape);
});

onUnmounted(() => {
  document.removeEventListener("click", handleDocumentClick);
  document.removeEventListener("keydown", handleEscape);
});

watch(() => route.fullPath, closeMenu);
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  height: 64px;
  background: linear-gradient(135deg, #0F2D32, #123237, #163B41);
  border-bottom: 1px solid rgba(233, 220, 186, 0.18);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  position: relative;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-accent-soft);
  text-decoration: none;
  font-weight: 700;
  font-size: 1rem;
}

.brand-icon { width: 24px; height: 24px; color: var(--color-accent); }

.nav-menu {
  position: relative;
}

.menu-toggle {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(233, 220, 186, 0.25);
  color: var(--color-accent-soft);
  border-radius: 8px;
  padding: 0.4rem 0.65rem;
  cursor: pointer;
  transition: background 0.18s, border-color 0.18s;
}

.menu-toggle:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(233, 220, 186, 0.45);
}

.menu-icon {
  font-size: 0.95rem;
  line-height: 1;
}

.menu-label {
  font-size: 0.85rem;
}

.menu-dropdown {
  position: absolute;
  left: 0;
  top: calc(100% + 0.45rem);
  min-width: 220px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  box-shadow: var(--app-shadow-card);
  overflow: hidden;
  z-index: 120;
}

.menu-item {
  display: block;
  color: var(--color-text);
  text-decoration: none;
  padding: 0.7rem 0.9rem;
  font-size: 0.9rem;
  transition: background 0.15s;
}

.menu-item:hover,
.menu-item.router-link-active {
  background: rgba(207, 163, 113, 0.13);
  color: var(--color-primary);
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-name { color: var(--color-accent-soft); font-size: 0.9rem; opacity: 0.85; }

.logout-btn {
  background: var(--color-accent-wine);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: var(--color-text-inverse);
  border-radius: 500px;
  padding: 0.35rem 1rem;
  font-size: 0.85rem;
  cursor: pointer;
  font-weight: 600;
  transition: opacity 0.2s;
}

.logout-btn:hover { opacity: 0.85; }

@media (max-width: 768px) {
  .navbar {
    padding: 0 1rem;
    gap: 0.75rem;
  }

  .user-name {
    display: none;
  }

  .menu-label {
    display: none;
  }

  .menu-dropdown {
    min-width: 190px;
  }
}
</style>
