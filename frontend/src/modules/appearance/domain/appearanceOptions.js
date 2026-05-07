export const DEFAULT_APPEARANCE_PREFERENCES = {
  themeMode: "system",
  accent: "spotify",
  visualVariant: "default",
};

export const THEME_MODE_OPTIONS = [
  { value: "system", label: "Sistema" },
  { value: "dark", label: "Oscuro" },
  { value: "light", label: "Claro" },
];

export const ACCENT_OPTIONS = [
  { value: "spotify", label: "Spotify", color: "#1db954", softColor: "#1ed760" },
  { value: "ocean", label: "Ocean", color: "#2e9bff", softColor: "#66b6ff" },
  { value: "sunset", label: "Sunset", color: "#ff8a3d", softColor: "#ffad75" },
];

export function normalizeThemeMode(value) {
  const normalizedValue = String(value || "").trim();
  const exists = THEME_MODE_OPTIONS.some((option) => option.value === normalizedValue);
  return exists ? normalizedValue : DEFAULT_APPEARANCE_PREFERENCES.themeMode;
}

export function normalizeAccent(value) {
  const normalizedValue = String(value || "").trim();
  const exists = ACCENT_OPTIONS.some((option) => option.value === normalizedValue);
  return exists ? normalizedValue : DEFAULT_APPEARANCE_PREFERENCES.accent;
}

export function normalizeVisualVariant(value) {
  const normalizedValue = String(value || "").trim();
  return normalizedValue || DEFAULT_APPEARANCE_PREFERENCES.visualVariant;
}

export function getAccentOption(accent) {
  const normalizedAccent = normalizeAccent(accent);
  return ACCENT_OPTIONS.find((option) => option.value === normalizedAccent) || ACCENT_OPTIONS[0];
}
