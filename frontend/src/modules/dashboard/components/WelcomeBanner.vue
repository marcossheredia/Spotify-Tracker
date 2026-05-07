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
  background: linear-gradient(135deg, #1db954, #117a37);
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
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
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
}

.greeting {
  font-size: 0.85rem;
  opacity: 0.8;
  margin-bottom: 0.2rem;
}

h2 {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 0.4rem;
}

.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 500px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  background: rgba(0, 0, 0, 0.3);
}
</style>