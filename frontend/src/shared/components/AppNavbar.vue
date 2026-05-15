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
          <RouterLink to="/library" class="menu-item" @click="closeMenu">
            Biblioteca
          </RouterLink>
          <RouterLink to="/followed-artists" class="menu-item" @click="closeMenu">
            Artistas Seguidos
          </RouterLink>
          <RouterLink to="/search" class="menu-item" @click="closeMenu">
            Busqueda
          </RouterLink>
          <!-- <RouterLink to="/player" class="menu-item" @click="closeMenu">
            Player
          </RouterLink> -->
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
      <div
        ref="profileRoot"
        class="profile-menu"
        @mouseenter="openProfileMenu"
        @mouseleave="closeProfileMenu"
      >
        <button
          type="button"
          class="profile-avatar-btn"
          :aria-expanded="String(profileOpen)"
          aria-controls="profile-dropdown"
          @click.stop="toggleProfileMenu"
        >
          <img
            v-if="profileData?.imageUrl"
            :src="profileData.imageUrl"
            :alt="profileData?.displayName || 'Usuario Spotify'"
            class="profile-avatar-img"
          />
          <span v-else class="profile-avatar-fallback">
            {{ profileInitial }}
          </span>
        </button>

        <div v-if="profileOpen" id="profile-dropdown" class="profile-dropdown">
          <p v-if="profileLoading" class="profile-dropdown-status">Cargando perfil...</p>
          <p v-else-if="profileError" class="profile-dropdown-error">{{ profileError }}</p>

          <div v-if="!profileLoading" class="profile-dropdown-body">
            <div class="profile-dropdown-header">
              <div class="profile-dropdown-avatar">
                <img
                  v-if="profileData?.imageUrl"
                  :src="profileData.imageUrl"
                  :alt="profileData?.displayName || 'Usuario Spotify'"
                />
                <span v-else>{{ profileInitial }}</span>
              </div>
              <div class="profile-dropdown-info">
                <p class="profile-dropdown-name">
                  {{ profileData?.displayName || "Usuario Spotify" }}
                </p>
                <p class="profile-dropdown-email">
                  {{ profileData?.email || "Email no disponible" }}
                </p>
              </div>
            </div>

            <div class="profile-dropdown-list">
              <div class="profile-dropdown-row">
                <span class="profile-dropdown-label">Pais</span>
                <span class="profile-dropdown-value">
                  {{ profileData?.country || "No disponible" }}
                </span>
              </div>
              <div class="profile-dropdown-row">
                <span class="profile-dropdown-label">Cuenta</span>
                <span class="profile-dropdown-value">
                  {{ profileData?.product || "No disponible" }}
                </span>
              </div>
              <div class="profile-dropdown-row">
                <span class="profile-dropdown-label">Seguidores</span>
                <span class="profile-dropdown-value">
                  {{ profileData?.followersTotal ?? "No disponible" }}
                </span>
              </div>
            </div>

            <a
              v-if="profileData?.externalUrl"
              :href="profileData.externalUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="profile-dropdown-link"
            >
              Abrir perfil en Spotify
            </a>

            <p v-if="profileData?.capabilitiesNote" class="profile-dropdown-note">
              {{ profileData.capabilitiesNote }}
            </p>
          </div>
        </div>
      </div>
      <button class="logout-btn" @click="logout">Cerrar sesión</button>
    </div>
  </nav>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "@/shared/composables/useAuth";
import { useProfile } from "@/modules/profile/composables/useProfile";

const { logout, authStore } = useAuth();
const user = computed(() => authStore.user);
const { profile, loading: profileLoading, error: profileError, loadProfile } = useProfile();
const profileData = computed(() => profile.value || user.value);
const profileInitial = computed(() => {
  const name = profileData.value?.displayName || "U";
  return name.trim().charAt(0).toUpperCase();
});
const route = useRoute();
const menuOpen = ref(false);
const menuRoot = ref(null);
const profileOpen = ref(false);
const profileRoot = ref(null);

function toggleMenu() {
  menuOpen.value = !menuOpen.value;
}

function closeMenu() {
  menuOpen.value = false;
}

function openProfileMenu() {
  profileOpen.value = true;
}

function closeProfileMenu() {
  profileOpen.value = false;
}

function toggleProfileMenu() {
  profileOpen.value = !profileOpen.value;
}

function handleDocumentClick(event) {
  const target = event.target;

  if (menuOpen.value && menuRoot.value && !menuRoot.value.contains(target)) {
    closeMenu();
  }

  if (profileOpen.value && profileRoot.value && !profileRoot.value.contains(target)) {
    closeProfileMenu();
  }
}

function handleEscape(event) {
  if (event.key === "Escape") {
    closeMenu();
    closeProfileMenu();
  }
}

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);
  document.addEventListener("keydown", handleEscape);
  loadProfile();
});

onUnmounted(() => {
  document.removeEventListener("click", handleDocumentClick);
  document.removeEventListener("keydown", handleEscape);
});

watch(() => route.fullPath, () => {
  closeMenu();
  closeProfileMenu();
});
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

.profile-menu {
  position: relative;
  display: flex;
  align-items: center;
}

.profile-avatar-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(233, 220, 186, 0.35);
  background: rgba(233, 220, 186, 0.08);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-accent-soft);
  cursor: pointer;
  padding: 0;
  transition: border-color 0.15s, transform 0.15s;
}

.profile-avatar-btn:hover {
  border-color: var(--color-accent);
  transform: translateY(-1px);
}

.profile-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.profile-avatar-fallback {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--color-accent-soft);
}

.profile-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 0.5rem);
  width: 280px;
  max-width: calc(100vw - 1.5rem);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: var(--app-shadow-card);
  padding: 0.85rem;
  z-index: 140;
  color: var(--color-text);
}

.profile-dropdown-header {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  margin-bottom: 0.65rem;
}

.profile-dropdown-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 1px solid var(--color-border-soft);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: var(--color-primary);
  font-weight: 700;
}

.profile-dropdown-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-dropdown-info {
  min-width: 0;
}

.profile-dropdown-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.profile-dropdown-email {
  font-size: 0.78rem;
  color: var(--color-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.profile-dropdown-list {
  display: grid;
  gap: 0.35rem;
  margin-bottom: 0.6rem;
}

.profile-dropdown-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  font-size: 0.78rem;
}

.profile-dropdown-label {
  color: var(--color-muted);
}

.profile-dropdown-value {
  color: var(--color-text);
  font-weight: 600;
}

.profile-dropdown-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-top: 0.25rem;
  padding: 0.35rem 0.6rem;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  color: var(--color-primary);
  font-size: 0.78rem;
  font-weight: 600;
  text-decoration: none;
  transition: border-color 0.15s;
}

.profile-dropdown-link:hover {
  border-color: var(--color-accent);
}

.profile-dropdown-note {
  margin-top: 0.6rem;
  font-size: 0.75rem;
  color: var(--color-muted);
}

.profile-dropdown-status {
  font-size: 0.8rem;
  color: var(--color-muted);
}

.profile-dropdown-error {
  font-size: 0.78rem;
  color: var(--color-accent-wine);
  margin-bottom: 0.4rem;
}

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

  .menu-label {
    display: none;
  }

  .menu-dropdown {
    min-width: 190px;
  }

  .profile-dropdown {
    width: min(320px, calc(100vw - 2rem));
  }
}
</style>
