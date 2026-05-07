import { computed, ref } from "vue";
import { defineStore } from "pinia";
import {
  ACCENT_OPTIONS,
  DEFAULT_APPEARANCE_PREFERENCES,
  THEME_MODE_OPTIONS,
  getAccentOption,
  normalizeAccent,
  normalizeThemeMode,
  normalizeVisualVariant,
} from "@/modules/appearance/domain/appearanceOptions";
import { appearancePreferencesStorage } from "@/modules/appearance/infrastructure/AppearancePreferencesStorage";

function applyToDocument({ accent, themeMode }) {
  if (typeof document === "undefined") {
    return;
  }

  const root = document.documentElement;
  const accentOption = getAccentOption(accent);
  root.style.setProperty("--color-accent", accentOption.color);
  root.style.setProperty("--color-accent-soft", accentOption.softColor);
  root.style.setProperty("--color-green", accentOption.color);
  root.style.setProperty("--color-green-l", accentOption.softColor);
  root.setAttribute("data-appearance-theme-mode", themeMode);
}

export const useAppearanceStore = defineStore("appearance", () => {
  const initialized = ref(false);
  const themeMode = ref(DEFAULT_APPEARANCE_PREFERENCES.themeMode);
  const accent = ref(DEFAULT_APPEARANCE_PREFERENCES.accent);
  const visualVariant = ref(DEFAULT_APPEARANCE_PREFERENCES.visualVariant);

  const accentOption = computed(() => getAccentOption(accent.value));

  function persistPreferences() {
    appearancePreferencesStorage.save({
      themeMode: themeMode.value,
      accent: accent.value,
      visualVariant: visualVariant.value,
    });
  }

  function initialize() {
    if (initialized.value) {
      return;
    }

    const storedPreferences = appearancePreferencesStorage.load();

    themeMode.value = normalizeThemeMode(storedPreferences?.themeMode);
    accent.value = normalizeAccent(storedPreferences?.accent);
    visualVariant.value = normalizeVisualVariant(storedPreferences?.visualVariant);

    applyToDocument({
      accent: accent.value,
      themeMode: themeMode.value,
    });

    initialized.value = true;
  }

  function setThemeMode(nextThemeMode) {
    themeMode.value = normalizeThemeMode(nextThemeMode);
    persistPreferences();
    applyToDocument({
      accent: accent.value,
      themeMode: themeMode.value,
    });
  }

  function setAccent(nextAccent) {
    accent.value = normalizeAccent(nextAccent);
    persistPreferences();
    applyToDocument({
      accent: accent.value,
      themeMode: themeMode.value,
    });
  }

  function setVisualVariant(nextVisualVariant) {
    visualVariant.value = normalizeVisualVariant(nextVisualVariant);
    persistPreferences();
  }

  return {
    initialized,
    themeMode,
    accent,
    visualVariant,
    accentOption,
    themeModeOptions: THEME_MODE_OPTIONS,
    accentOptions: ACCENT_OPTIONS,
    initialize,
    setThemeMode,
    setAccent,
    setVisualVariant,
  };
});
