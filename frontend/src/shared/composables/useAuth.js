import { useAuthStore } from "@/stores/authStore";
import { authService } from "@/shared/services/authService";
import { useRouter } from "vue-router";

export function useAuth() {
  const authStore = useAuthStore();
  const router    = useRouter();

  function login() {
    authService.loginWithSpotify();
  }

  function logout() {
    authStore.logout();
    router.push({ name: "Home" });
  }

  return { login, logout, authStore };
}
