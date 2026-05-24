const STORAGE_KEY = "spotify_tracker_appearance_preferences";

/**
 * Clase funcional: AppearancePreferencesStorage.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras utilidades del frontend.
 */
export class AppearancePreferencesStorage {
  /** Ejecuta una parte concreta de la lógica de esta clase. */
  load() {
    if (typeof window === "undefined") {
      return null;
    }

    const rawValue = window.localStorage.getItem(STORAGE_KEY);
    if (!rawValue) {
      return null;
    }

    try {
      const parsed = JSON.parse(rawValue);
      return parsed && typeof parsed === "object" ? parsed : null;
    } catch {
      return null;
    }
  }

  /** Guarda o actualiza datos en el sistema. */

  save(preferences) {
    if (typeof window === "undefined") {
      return;
    }

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
  }
}

export const appearancePreferencesStorage = new AppearancePreferencesStorage();
