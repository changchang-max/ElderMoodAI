<template>
  <aside class="app-sidebar" :class="{ collapsed }">
    <!-- 折叠按钮 -->
    <div class="collapse-btn" @click="$emit('toggle')">
      <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
    </div>

    <el-menu
      :default-active="activeRoute"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="#1a1a2e"
      text-color="#c0c4cc"
      active-text-color="#FF6B35"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><House /></el-icon>
        <template #title>首页概览</template>
      </el-menu-item>

      <el-menu-item index="/monitor">
        <el-icon><VideoCamera /></el-icon>
        <template #title>实时情感监测</template>
      </el-menu-item>

      <el-menu-item index="/visualization">
        <el-icon><TrendCharts /></el-icon>
        <template #title>历史数据可视化</template>
      </el-menu-item>

      <el-menu-item index="/alerts">
        <el-icon><Bell /></el-icon>
        <template #title>
          预警通知
          <el-badge v-if="pendingCount > 0" :value="pendingCount" class="alert-badge" />
        </template>
      </el-menu-item>

      <el-menu-item v-if="canAccessElders" index="/elders">
        <el-icon><User /></el-icon>
        <template #title>老人信息管理</template>
      </el-menu-item>

      <el-menu-item index="/settings">
        <el-icon><Setting /></el-icon>
        <template #title>系统设置</template>
      </el-menu-item>

      <el-menu-item v-if="isAdmin" index="/admin">
        <el-icon><Tools /></el-icon>
        <template #title>管理后台</template>
      </el-menu-item>

      <el-menu-item index="/help">
        <el-icon><QuestionFilled /></el-icon>
        <template #title>帮助中心</template>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  House, VideoCamera, TrendCharts, Bell, User,
  Setting, Tools, QuestionFilled, Fold, Expand
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth.js'
import { getPendingCount } from '@/mock/alerts.js'

defineProps({ collapsed: Boolean })
defineEmits(['toggle'])

const route = useRoute()
const authStore = useAuthStore()

const activeRoute = computed(() => route.path)
const isAdmin = computed(() => authStore.isAdmin)
const canAccessElders = computed(() => authStore.role !== 'family')
const pendingCount = computed(() => getPendingCount())
</script>

<style lang="scss" scoped>
.app-sidebar {
  position: fixed;
  top: $header-height;
  left: 0;
  bottom: 0;
  width: $sidebar-width;
  background: #1a1a2e;
  transition: width 0.3s ease;
  z-index: 999;
  overflow: hidden;

  &.collapsed {
    width: $sidebar-collapsed-width;
  }

  .el-menu {
    border-right: none;
    height: calc(100% - 40px);
    overflow-y: auto;
    overflow-x: hidden;
  }

  :deep(.el-menu-item) {
    height: 50px;
    line-height: 50px;
    &:hover { background-color: rgba(255, 107, 53, 0.15) !important; }
    &.is-active { background-color: rgba(255, 107, 53, 0.2) !important; }
  }

  .collapse-btn {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding-right: 16px;
    cursor: pointer;
    color: #909399;
    border-bottom: 1px solid rgba(255,255,255,0.05);
    &:hover { color: $primary-color; }
  }

  .alert-badge {
    margin-left: 8px;
    :deep(.el-badge__content) { background: $alert-high; }
  }
}
</style>
