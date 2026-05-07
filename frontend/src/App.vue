<template>
  <RouterView v-slot="{ Component, route }">
    <component :is="route.meta.requiresAuth ? AppLayout : AuthLayout">
      <component :is="Component" />
    </component>
  </RouterView>
</template>

<script setup>
import { onMounted } from "vue";
import AppLayout from "@/layouts/AppLayout.vue";
import AuthLayout from "@/layouts/AuthLayout.vue";
import { useAppearanceStore } from "@/stores/appearanceStore";

const appearanceStore = useAppearanceStore();

onMounted(() => {
  appearanceStore.initialize();
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
