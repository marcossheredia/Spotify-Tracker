import axios from "axios";
import { getBackendBaseUrl } from "./runtimeConfig";

const api = axios.create({
  baseURL: getBackendBaseUrl(),
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor: añadir token JWT a cada petición
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("spotify_tracker_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor: manejar errores 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const details = error.response?.data?.details;

    if (status === 401) {
      localStorage.removeItem("spotify_tracker_token");
      localStorage.removeItem("spotify_tracker_user");
      window.location.href = "/";
    }

    if (status === 429 && Array.isArray(details)) {
      const retryDetail = details.find((detail) => String(detail).startsWith("retry_after_seconds="));
      if (retryDetail) {
        const seconds = Number(String(retryDetail).split("=")[1]);
        error.retryAfterSeconds = Number.isFinite(seconds) ? seconds : null;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
