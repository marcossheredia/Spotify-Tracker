import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { authService } from "@/shared/services/authService";

export const useAuthStore = defineStore("auth", () => {
  const token = ref(localStorage.getItem("spotify_tracker_token") || null);
  const user  = ref(JSON.parse(localStorage.getItem("spotify_tracker_user") || "null"));

  const isAuthenticated = computed(() => !!token.value);

  function setToken(newToken) {
    token.value = newToken;
    localStorage.setItem("spotify_tracker_token", newToken);
  }

  function setUser(newUser) {
    user.value = newUser;
    localStorage.setItem("spotify_tracker_user", JSON.stringify(newUser));
  }

  function logout() {
    token.value = null;
    user.value  = null;
    localStorage.removeItem("spotify_tracker_token");
    localStorage.removeItem("spotify_tracker_user");
  }

  async function fetchCurrentUser() {
    try {
      const data = await authService.getMe();
      setUser(data);
    } catch {
      logout();
    }
  }

  return { token, user, isAuthenticated, setToken, setUser, logout, fetchCurrentUser };
});
