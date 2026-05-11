<template>
  <section class="playlist-toolbar">
    <div class="toolbar-top-row">
      <label class="search-wrapper" for="playlist-search-input">
        <span class="search-icon">⌕</span>
        <input
          id="playlist-search-input"
          v-model="localSearch"
          type="search"
          placeholder="Buscar por playlist u owner"
          autocomplete="off"
        />
      </label>

      <div class="sort-wrapper">
        <label for="playlist-sort-select">Orden</label>
        <select
          id="playlist-sort-select"
          :value="sortBy"
          @change="emitSortChange"
        >
          <option value="recent">Recientes primero</option>
          <option value="name">Nombre A-Z</option>
          <option value="tracks">Mas canciones</option>
          <option value="lastPlayed">Ultima reproduccion</option>
        </select>
      </div>

      <div class="view-toggle" role="group" aria-label="Modo de visualizacion">
        <button
          type="button"
          class="view-btn"
          :class="{ active: viewMode === 'list' }"
          title="Vista lista"
          aria-label="Vista lista"
          @click="emitViewModeChange('list')"
        >
          <span class="view-icon view-icon-list">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>
        <button
          type="button"
          class="view-btn"
          :class="{ active: viewMode === 'grid' }"
          title="Vista grid"
          aria-label="Vista grid"
          @click="emitViewModeChange('grid')"
        >
          <span class="view-icon view-icon-grid">
            <span></span>
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>
      </div>
    </div>

    <div class="toolbar-chips">
      <button
        v-for="chip in chips"
        :key="chip.value"
        type="button"
        class="chip"
        :class="{ active: activeFilter === chip.value }"
        @click="emitFilterChange(chip.value)"
      >
        {{ chip.label }}
      </button>
    </div>
  </section>
</template>

<script setup>
import { onUnmounted, ref, watch } from "vue";

const props = defineProps({
  searchQuery: {
    type: String,
    default: "",
  },
  sortBy: {
    type: String,
    default: "recent",
  },
  activeFilter: {
    type: String,
    default: "all",
  },
  viewMode: {
    type: String,
    default: "list",
  },
});

const emit = defineEmits([
  "update:searchQuery",
  "update:sortBy",
  "update:activeFilter",
  "update:viewMode",
]);

const chips = [
  { value: "all", label: "Todas" },
  { value: "recent", label: "Recientes" },
  { value: "liked", label: "Con favoritas" },
  { value: "owned", label: "Propias" },
  { value: "collab", label: "Colaborativas" },
];

const localSearch = ref(props.searchQuery);
let debounceTimer = null;

watch(() => props.searchQuery, (newValue) => {
  if (newValue !== localSearch.value) {
    localSearch.value = newValue;
  }
});

watch(localSearch, (newValue) => {
  if (debounceTimer) {
    window.clearTimeout(debounceTimer);
  }

  debounceTimer = window.setTimeout(() => {
    emit("update:searchQuery", newValue);
  }, 180);
});

function emitSortChange(event) {
  emit("update:sortBy", event.target.value);
}

function emitFilterChange(nextFilter) {
  if (nextFilter !== props.activeFilter) {
    emit("update:activeFilter", nextFilter);
  }
}

function emitViewModeChange(nextViewMode) {
  if (nextViewMode !== props.viewMode) {
    emit("update:viewMode", nextViewMode);
  }
}

onUnmounted(() => {
  if (debounceTimer) {
    window.clearTimeout(debounceTimer);
  }
});
</script>

<style scoped>
.playlist-toolbar {
  position: sticky;
  top: 64px;
  z-index: 20;
  background: rgba(253, 248, 239, 0.92);
  backdrop-filter: blur(7px);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 0.9rem;
  margin-bottom: 1rem;
  box-shadow: var(--app-shadow-soft);
}

.toolbar-top-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 0.75rem;
  align-items: center;
}

.search-wrapper {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 0.45rem;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: 10px;
  padding: 0.5rem 0.65rem;
  transition: border-color 0.18s;
}

.search-wrapper:focus-within {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(207, 163, 113, 0.18);
}

.search-icon {
  color: var(--color-muted-soft);
  font-size: 0.95rem;
}

.search-wrapper input {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  color: var(--color-text);
  font-size: 0.9rem;
}

.sort-wrapper {
  display: flex;
  align-items: center;
  gap: 0.45rem;
}

.sort-wrapper label {
  color: var(--color-muted);
  font-size: 0.8rem;
}

.sort-wrapper select {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  border-radius: 9px;
  padding: 0.45rem 0.55rem;
  font-size: 0.85rem;
}

.view-toggle {
  display: inline-flex;
  border: 1px solid var(--color-border);
  border-radius: 9px;
  overflow: hidden;
}

.view-btn {
  border: none;
  background: var(--color-surface);
  color: var(--color-muted);
  padding: 0.45rem 0.6rem;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.view-btn.active {
  background: var(--color-surface-strong);
  color: var(--color-primary);
}

.view-icon {
  display: inline-grid;
  width: 18px;
  height: 18px;
  gap: 3px;
}

.view-icon-list {
  grid-template-rows: repeat(3, 1fr);
}

.view-icon-list span {
  display: block;
  height: 3px;
  border-radius: 999px;
  background: currentColor;
}

.view-icon-grid {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.view-icon-grid span {
  display: block;
  border-radius: 4px;
  background: currentColor;
}

.toolbar-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.chip {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-muted);
  border-radius: 999px;
  padding: 0.34rem 0.7rem;
  font-size: 0.78rem;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, color 0.15s;
}

.chip:hover {
  border-color: var(--color-accent);
}

.chip.active {
  border-color: var(--color-accent);
  background: rgba(207, 163, 113, 0.16);
  color: var(--color-primary);
}

@media (max-width: 900px) {
  .toolbar-top-row {
    grid-template-columns: 1fr;
  }

  .sort-wrapper {
    justify-content: space-between;
  }

  .view-toggle {
    width: fit-content;
  }
}
</style>
