<template>
  <div class="app-layout">
    <AppHeader />
    <AppSidebar :collapsed="sidebarCollapsed" @toggle="sidebarCollapsed = !sidebarCollapsed" />
    <main class="main-content" :class="{ collapsed: sidebarCollapsed }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'

const sidebarCollapsed = ref(false)
</script>

<style lang="scss" scoped>
.app-layout {
  min-height: 100vh;
  background: $bg-color;
}

.main-content {
  margin-top: $header-height;
  margin-left: $sidebar-width;
  padding: $spacing-lg;
  min-height: calc(100vh - #{$header-height});
  transition: margin-left 0.3s ease;

  &.collapsed {
    margin-left: $sidebar-collapsed-width;
  }

  @media (max-width: 768px) {
    margin-left: 0;
    padding: $spacing-md;
  }
}
</style>
