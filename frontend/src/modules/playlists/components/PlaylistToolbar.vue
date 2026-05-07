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
          @click="emitViewModeChange('list')"
        >
          Lista
        </button>
        <button
          type="button"
          class="view-btn"
          :class="{ active: viewMode === 'grid' }"
          @click="emitViewModeChange('grid')"
        >
          Grid
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
  background: rgba(18, 18, 18, 0.92);
  backdrop-filter: blur(7px);
  border: 1px solid #2a2a2a;
  border-radius: 12px;
  padding: 0.9rem;
  margin-bottom: 1rem;
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
  border: 1px solid #343434;
  background: #171717;
  border-radius: 10px;
  padding: 0.5rem 0.65rem;
}

.search-icon {
  color: #9f9f9f;
  font-size: 0.95rem;
}

.search-wrapper input {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  color: #fff;
  font-size: 0.9rem;
}

.sort-wrapper {
  display: flex;
  align-items: center;
  gap: 0.45rem;
}

.sort-wrapper label {
  color: #afafaf;
  font-size: 0.8rem;
}

.sort-wrapper select {
  background: #1c1c1c;
  border: 1px solid #343434;
  color: #fff;
  border-radius: 9px;
  padding: 0.45rem 0.55rem;
  font-size: 0.85rem;
}

.view-toggle {
  display: inline-flex;
  border: 1px solid #3a3a3a;
  border-radius: 9px;
  overflow: hidden;
}

.view-btn {
  border: none;
  background: #1a1a1a;
  color: #bdbdbd;
  padding: 0.45rem 0.7rem;
  font-size: 0.82rem;
  cursor: pointer;
}

.view-btn.active {
  background: #2c2c2c;
  color: #fff;
}

.toolbar-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.chip {
  border: 1px solid #3a3a3a;
  background: #1b1b1b;
  color: #d1d1d1;
  border-radius: 999px;
  padding: 0.34rem 0.7rem;
  font-size: 0.78rem;
  cursor: pointer;
}

.chip.active {
  border-color: var(--color-accent, #1db954);
  background: rgba(29, 185, 84, 0.16);
  color: #fff;
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
