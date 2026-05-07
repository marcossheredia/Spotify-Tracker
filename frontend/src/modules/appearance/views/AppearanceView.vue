<template>
  <div class="appearance-view">
    <header class="view-header">
      <h1>Apariencia</h1>
      <p>Configura opciones visuales que se guardan de forma local.</p>
    </header>

    <section class="appearance-card">
      <h2>Preferencias visuales</h2>

      <div class="field-row">
        <label for="appearance-theme">Modo de tema (base escalable)</label>
        <select
          id="appearance-theme"
          :value="appearanceStore.themeMode"
          @change="onThemeModeChange"
        >
          <option
            v-for="option in appearanceStore.themeModeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <p class="field-help">Se guarda desde hoy para permitir ampliar temas sin rehacer estructura.</p>
      </div>

      <div class="field-row">
        <label for="appearance-accent">Color de acento</label>
        <select
          id="appearance-accent"
          :value="appearanceStore.accent"
          @change="onAccentChange"
        >
          <option
            v-for="option in appearanceStore.accentOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </div>

      <div class="preview-box">
        <span class="preview-chip">Vista previa de acento</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useAppearanceStore } from "@/stores/appearanceStore";

const appearanceStore = useAppearanceStore();

function onThemeModeChange(event) {
  appearanceStore.setThemeMode(event.target.value);
}

function onAccentChange(event) {
  appearanceStore.setAccent(event.target.value);
}
</script>

<style scoped>
.appearance-view {
  min-height: calc(100vh - 64px);
  color: var(--color-text);
}

.view-header {
  margin-bottom: 1.25rem;
}

.view-header h1 {
  font-size: 1.5rem;
  margin-bottom: 0.35rem;
  font-weight: 700;
  color: var(--color-text);
}

.view-header p {
  color: var(--color-muted);
}

.appearance-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--app-radius-md);
  padding: 1.5rem;
  max-width: 680px;
  box-shadow: var(--app-shadow-soft);
}

.appearance-card h2 {
  margin-bottom: 1rem;
  font-size: 1.1rem;
  color: var(--color-text);
}

.field-row {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
  margin-bottom: 1rem;
}

.field-row label {
  color: var(--color-muted);
  font-size: 0.9rem;
}

.field-help {
  color: var(--color-muted-soft);
  font-size: 0.8rem;
}

.field-row select {
  width: 100%;
  max-width: 260px;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  border-radius: 8px;
  padding: 0.5rem 0.6rem;
  font-family: inherit;
  transition: border-color 0.18s;
}

.field-row select:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(207, 163, 113, 0.18);
}

.preview-box {
  margin-top: 1.2rem;
  padding: 1rem;
  border-radius: 10px;
  background: var(--color-surface-soft);
  border: 1px dashed var(--color-border);
}

.preview-chip {
  display: inline-flex;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  background: var(--color-accent);
  color: var(--color-primary);
  font-weight: 700;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .appearance-view {
    padding: 0;
  }

  .appearance-card {
    padding: 1rem;
  }
}
</style>
