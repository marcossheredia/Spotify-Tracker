import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/authStore";

const routes = [
  { path: "/", name: "Home", component: () => import("@/modules/auth/views/LoginView.vue"), meta: { requiresAuth: false } },
  { path: "/auth/callback", name: "AuthCallback", component: () => import("@/modules/auth/views/CallbackView.vue"), meta: { requiresAuth: false } },
  { path: "/dashboard", name: "Dashboard", component: () => import("@/modules/dashboard/views/DashboardView.vue"), meta: { requiresAuth: true } },
  { path: "/playtime", name: "Playtime", component: () => import("@/modules/playback/views/PlaytimeView.vue"), meta: { requiresAuth: true } },
  { path: "/playlists", name: "Playlists", component: () => import("@/modules/playlists/views/PlaylistsView.vue"), meta: { requiresAuth: true } },
  { path: "/profile", name: "Profile", component: () => import("@/modules/profile/views/ProfileView.vue"), meta: { requiresAuth: true } },
  { path: "/library", name: "Library", component: () => import("@/modules/library/views/LibraryView.vue"), meta: { requiresAuth: true } },
  { path: "/followed-artists", name: "FollowedArtists", component: () => import("@/modules/followedArtists/views/FollowedArtistsView.vue"), meta: { requiresAuth: true } },
  { path: "/search", name: "Search", component: () => import("@/modules/search/views/SearchView.vue"), meta: { requiresAuth: true } },
  { path: "/player", name: "Player", component: () => import("@/modules/player/views/PlayerView.vue"), meta: { requiresAuth: true } },
  { path: "/automation", name: "Automation", component: () => import("@/modules/automation/views/AutomationView.vue"), meta: { requiresAuth: true } },
  { path: "/appearance", name: "Appearance", component: () => import("@/modules/appearance/views/AppearanceView.vue"), meta: { requiresAuth: true } },
  { path: "/assistant", name: "Assistant", component: () => import("@/modules/assistant/views/AssistantView.vue"), meta: { requiresAuth: true } },
  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach((to) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: "Home" };
  }

  if (to.name === "Home" && authStore.isAuthenticated) {
    return { name: "Dashboard" };
  }

  return true;
});

export default router;
