import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginByEmail, loginByPhone, logout as apiLogout } from '@/api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('elder_user') || 'null'))
  const token = ref(localStorage.getItem('elder_token') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const role = computed(() => user.value?.role || '')
  const isAdmin = computed(() => role.value === 'admin' || role.value === 'ADMIN')
  const isCaregiver = computed(() => role.value === 'caregiver' || role.value === 'CAREGIVER')
  const isFamily = computed(() => role.value === 'family' || role.value === 'FAMILY' || role.value === 'GUARDIAN')

  // 角色权限映射
  const rolePermissions = {
    admin:     ['dashboard', 'monitor', 'visualization', 'alerts', 'elders', 'settings', 'admin', 'help'],
    ADMIN:     ['dashboard', 'monitor', 'visualization', 'alerts', 'elders', 'settings', 'admin', 'help'],
    caregiver: ['dashboard', 'monitor', 'visualization', 'alerts', 'elders', 'settings', 'help'],
    CAREGIVER: ['dashboard', 'monitor', 'visualization', 'alerts', 'elders', 'settings', 'help'],
    family:    ['dashboard', 'monitor', 'visualization', 'alerts', 'settings', 'help'],
    FAMILY:    ['dashboard', 'monitor', 'visualization', 'alerts', 'settings', 'help'],
    GUARDIAN:  ['dashboard', 'monitor', 'visualization', 'alerts', 'settings', 'help'],
  }

  function hasPermission(page) {
    if (!role.value) return false
    return rolePermissions[role.value]?.includes(page) ?? false
  }

  async function login(credentials, type = 'email') {
    try {
      const res = type === 'email' 
        ? await loginByEmail(credentials) 
        : await loginByPhone(credentials)
      
      // 兼容两种响应格式
      // 后端格式: { success: true, message: '', data: User }
      // Mock格式: { code: 200, data: { accessToken, user } }
      
      if (res.success) {
        // 后端真实响应格式 - 登录功能尚未实现，暂时跳过
        return { success: false, message: '登录功能尚未实现，请使用演示账号' }
      } else if (res.code === 200) {
        // Mock响应格式
        token.value = res.data.accessToken
        user.value = res.data.user
        localStorage.setItem('elder_token', token.value)
        localStorage.setItem('elder_user', JSON.stringify(user.value))
        return { success: true }
      }
      return { success: false, message: res.message || '登录失败' }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '登录失败，请稍后重试' }
    }
  }

  async function logout() {
    try { 
      await apiLogout() 
    } catch {}
    token.value = ''
    user.value = null
    localStorage.removeItem('elder_token')
    localStorage.removeItem('elder_user')
  }

  function updateUser(updates) {
    user.value = { ...user.value, ...updates }
    localStorage.setItem('elder_user', JSON.stringify(user.value))
  }

  return { user, token, isLoggedIn, role, isAdmin, isCaregiver, isFamily, hasPermission, login, logout, updateUser }
})
