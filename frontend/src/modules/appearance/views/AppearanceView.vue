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
  background: #121212;
  color: #fff;
  padding: 2rem;
}

.view-header {
  margin-bottom: 1.25rem;
}

.view-header h1 {
  font-size: 1.5rem;
  margin-bottom: 0.35rem;
}

.view-header p {
  color: #b3b3b3;
}

.appearance-card {
  background: #282828;
  border: 1px solid #383838;
  border-radius: 12px;
  padding: 1.5rem;
  max-width: 680px;
}

.appearance-card h2 {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.field-row {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
  margin-bottom: 1rem;
}

.field-row label {
  color: #b3b3b3;
  font-size: 0.9rem;
}

.field-help {
  color: #9a9a9a;
  font-size: 0.8rem;
}

.field-row select {
  width: 100%;
  max-width: 260px;
  background: #1f1f1f;
  border: 1px solid #383838;
  color: #fff;
  border-radius: 8px;
  padding: 0.5rem 0.6rem;
}

.preview-box {
  margin-top: 1.2rem;
  padding: 1rem;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px dashed #525252;
}

.preview-chip {
  display: inline-flex;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  background: var(--color-accent);
  color: #0d0d0d;
  font-weight: 700;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .appearance-view {
    padding: 1rem;
  }

  .appearance-card {
    padding: 1rem;
  }
}
</style>
