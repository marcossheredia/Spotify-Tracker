import { defineStore } from "pinia";

export const useUiStore = defineStore("ui", {
  state: () => ({ sidebarOpen: false, globalLoading: false }),
  actions: {
    toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; },
    closeSidebar() { this.sidebarOpen = false; },
    setGlobalLoading(value) { this.globalLoading = Boolean(value); },
  },
});
