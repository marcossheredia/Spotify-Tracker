import api from "@/shared/services/httpClient";
import { getBackendBaseUrl, getFrontendBaseUrl } from "./runtimeConfig";

export const authService = {
  /**
   * Inicia el flujo OAuth2 redirigiendo al backend.
   */
  loginWithSpotify() {
    const backendUrl = getBackendBaseUrl();
    const loginUrl = new URL("/oauth2/authorization/spotify", backendUrl);
    loginUrl.searchParams.set("frontend_url", getFrontendBaseUrl());
    window.location.href = loginUrl.toString();
  },

  /**
   * Obtiene el usuario autenticado actualmente.
   */
  async getMe() {
    const { data } = await api.get("/api/auth/me");
    return data;
  },
};
