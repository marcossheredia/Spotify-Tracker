const STORAGE_KEY = "spotify_tracker_appearance_preferences";

export class AppearancePreferencesStorage {
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

  save(preferences) {
    if (typeof window === "undefined") {
      return;
    }

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
  }
}

export const appearancePreferencesStorage = new AppearancePreferencesStorage();
