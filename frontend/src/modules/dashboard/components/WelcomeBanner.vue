<template>
  <div class="dashboard-header">
    <div class="welcome">
      <img v-if="user?.imageUrl" :src="user.imageUrl" :alt="user.displayName" class="avatar" />
      <div v-else class="avatar-placeholder">{{ initials }}</div>

      <div>
        <p class="greeting">Bienvenido de vuelta</p>
        <h2>{{ user?.displayName || "Usuario" }}</h2>
        <span class="badge" :class="user?.product">{{ user?.product || "free" }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  user: {
    type: Object,
    default: null,
  },
  initials: {
    type: String,
    default: "",
  },
});
</script>

<style scoped>
.dashboard-header {
  background:
    radial-gradient(circle at top right, rgba(207, 163, 113, 0.22), transparent 38%),
    linear-gradient(135deg, var(--color-primary), var(--color-primary-strong));
  border-radius: var(--app-radius-lg);
  padding: 2rem;
  margin-bottom: 2rem;
  color: var(--color-text-inverse);
}

.welcome {
  display: flex;
  align-items: center;
  gap: 1.2rem;
}

.avatar,
.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  background: rgba(233, 220, 186, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-accent-soft);
}

.greeting {
  font-size: 0.85rem;
  opacity: 0.75;
  margin-bottom: 0.2rem;
  color: var(--color-accent-soft);
}

h2 {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 0.4rem;
  color: var(--color-text-inverse);
}

.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 500px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  background: rgba(207, 163, 113, 0.25);
  color: var(--color-accent-soft);
  border: 1px solid rgba(233, 220, 186, 0.3);
}
</style>