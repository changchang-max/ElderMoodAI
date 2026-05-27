<template>
  <header class="app-header">
    <!-- 左侧：Logo + 项目名 -->
    <div class="header-left">
      <div class="logo">
        <span class="logo-icon">🏠</span>
        <span class="logo-text">居家老人情感分析及可视化系统</span>
      </div>
    </div>

    <!-- 右侧：通知 + 用户信息 -->
    <div class="header-right">
      <!-- 消息通知 -->
      <el-popover placement="bottom-end" :width="320" trigger="click">
        <template #reference>
          <el-badge :value="unreadCount || ''" :hidden="unreadCount === 0" class="notify-badge">
            <el-button :icon="Bell" circle text size="large" class="icon-btn" />
          </el-badge>
        </template>
        <div class="notify-panel">
          <div class="notify-header">
            <span>消息通知</span>
            <el-button text size="small" @click="markAllRead">全部已读</el-button>
          </div>
          <div v-if="notifications.length === 0" class="notify-empty">暂无通知</div>
          <div v-else class="notify-list">
            <div
              v-for="n in notifications.slice(0, 5)"
              :key="n.id"
              class="notify-item"
              :class="{ unread: !n.read }"
              @click="markRead(n.id)"
            >
              <el-icon class="notify-icon" :style="{ color: n.alertLevel === 'high' ? '#F56C6C' : '#E6A23C' }">
                <Warning />
              </el-icon>
              <div class="notify-content">
                <p class="notify-msg">{{ n.message }}</p>
                <p class="notify-time">{{ formatTime(n.createdAt) }}</p>
              </div>
            </div>
          </div>
          <div class="notify-footer">
            <el-button text size="small" @click="$router.push('/alerts')">查看全部预警</el-button>
          </div>
        </div>
      </el-popover>

      <!-- 用户信息 -->
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="user?.avatar">
            {{ user?.name?.charAt(0) }}
          </el-avatar>
          <span class="user-name">{{ user?.name }}</span>
          <el-tag :type="roleTagType" size="small" class="role-tag">{{ roleLabel }}</el-tag>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="settings" :icon="Setting">个人设置</el-dropdown-item>
            <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Warning, ArrowDown, Setting, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth.js'
import { useNotificationStore } from '@/stores/notification.js'

const router = useRouter()
const authStore = useAuthStore()
const notifyStore = useNotificationStore()

const user = computed(() => authStore.user)
const notifications = computed(() => notifyStore.notifications)
const unreadCount = computed(() => notifyStore.unreadCount)

const roleLabel = computed(() => {
  const map = { admin: '管理员', caregiver: '护理员', family: '家属' }
  return map[user.value?.role] || ''
})
const roleTagType = computed(() => {
  const map = { admin: 'danger', caregiver: 'warning', family: 'success' }
  return map[user.value?.role] || ''
})

function markRead(id) { notifyStore.markRead(id) }
function markAllRead() { notifyStore.markAllRead() }

function formatTime(iso) {
  const d = new Date(iso)
  const now = new Date()
  const diff = Math.floor((now - d) / 60000)
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return d.toLocaleDateString('zh-CN')
}

async function handleCommand(cmd) {
  if (cmd === 'settings') {
    router.push('/settings')
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: $header-height;
  background: $header-bg;
  border-bottom: 1px solid $border-light;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;
  z-index: 1000;
  box-shadow: $shadow-sm;
}

.header-left {
  .logo {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    .logo-icon { font-size: 24px; }
    .logo-text {
      font-size: 16px;
      font-weight: 700;
      color: $primary-color;
      white-space: nowrap;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.icon-btn { color: $text-regular; }

.notify-badge :deep(.el-badge__content) {
  background-color: $alert-high;
}

.notify-panel {
  .notify-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border-light;
    font-weight: 600;
  }
  .notify-empty {
    text-align: center;
    color: $text-secondary;
    padding: $spacing-lg 0;
  }
  .notify-list {
    max-height: 280px;
    overflow-y: auto;
  }
  .notify-item {
    display: flex;
    gap: $spacing-sm;
    padding: $spacing-sm 0;
    border-bottom: 1px solid $border-light;
    cursor: pointer;
    &.unread { background: rgba($primary-color, 0.04); border-radius: $border-radius-sm; padding: $spacing-sm; }
    &:last-child { border-bottom: none; }
    .notify-icon { font-size: 18px; margin-top: 2px; flex-shrink: 0; }
    .notify-content {
      .notify-msg { font-size: 13px; color: $text-primary; line-height: 1.4; }
      .notify-time { font-size: 12px; color: $text-secondary; margin-top: 2px; }
    }
  }
  .notify-footer {
    text-align: center;
    padding-top: $spacing-sm;
    border-top: 1px solid $border-light;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  cursor: pointer;
  padding: 4px $spacing-sm;
  border-radius: $border-radius-md;
  transition: background 0.2s;
  &:hover { background: $bg-color; }
  .user-name { font-size: 14px; color: $text-primary; }
  .role-tag { margin-left: 2px; }
}
</style>
